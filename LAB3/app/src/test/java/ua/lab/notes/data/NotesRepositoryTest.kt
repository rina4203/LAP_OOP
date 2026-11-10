package ua.lab.notes.data

import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.lab.notes.data.auth.UnavailableAuthService
import ua.lab.notes.data.local.NoteEntity
import ua.lab.notes.data.remote.RemoteNote
import ua.lab.notes.data.sync.SyncResult
import ua.lab.notes.fakes.FakeAuth
import ua.lab.notes.fakes.FakeClock
import ua.lab.notes.fakes.FakeNoteDao
import ua.lab.notes.fakes.FakeRemote
import ua.lab.notes.fakes.entities

class NotesRepositoryTest {

    private val dao = FakeNoteDao()
    private val remote = FakeRemote()
    private val auth = FakeAuth()
    private val clock = FakeClock()
    private var nextId = 0
    private val repository = NotesRepository(dao, remote, auth, clock) { "id${++nextId}" }

    @Test
    fun savesNewNoteWithGeneratedIdAndTrimmedTitle() = runTest {
        val note = repository.saveNote(null, "  Shopping ", "milk")

        assertEquals(Note("id1", "Shopping", "milk", 1_000L), note)
        assertEquals(listOf(note), repository.observeNotes().first())
        assertEquals(1, repository.observePendingChanges().first())
    }

    @Test
    fun updatesExistingNote() = runTest {
        repository.saveNote(null, "Draft", "")
        clock.time = 2_000L

        repository.saveNote("id1", "Final", "text")

        assertEquals(Note("id1", "Final", "text", 2_000L), repository.getNote("id1"))
    }

    @Test
    fun deletedNoteDisappearsButStaysAsTombstone() = runTest {
        repository.saveNote(null, "Old", "")
        clock.time = 3_000L

        repository.deleteNote("id1")

        assertNull(repository.getNote("id1"))
        assertTrue(repository.observeNotes().first().isEmpty())
        val tombstone = dao.notes.entities.single()
        assertTrue(tombstone.deleted)
        assertEquals(3_000L, tombstone.updatedAt)
    }

    @Test
    fun deletingUnknownNoteDoesNothing() = runTest {
        repository.deleteNote("missing")

        assertTrue(dao.notes.value.isEmpty())
    }

    @Test
    fun syncIsUnavailableWithoutCloud() = runTest {
        val offline = NotesRepository(dao, null, UnavailableAuthService, clock)

        assertEquals(SyncResult.Unavailable, offline.sync())
    }

    @Test
    fun syncRequiresSignIn() = runTest {
        assertEquals(SyncResult.NotSignedIn, repository.sync())
    }

    @Test
    fun syncUploadsLocalAndDownloadsCloudNotes() = runTest {
        auth.signInAs("user")
        repository.saveNote(null, "Local", "")
        remote.notes["user"] = mutableMapOf(
            "cloud" to RemoteNote("cloud", "Cloud", "", 500L, false),
        )

        val result = repository.sync()

        assertEquals(SyncResult.Success(uploaded = 1, downloaded = 1), result)
        assertEquals(setOf("id1", "cloud"), remote.notes.getValue("user").keys)
        assertTrue(dao.notes.entities.all(NoteEntity::synced))
        assertEquals(0, repository.observePendingChanges().first())
    }

    @Test
    fun syncPurgesUploadedTombstonesAndAppliesCloudDeletions() = runTest {
        auth.signInAs("user")
        repository.saveNote(null, "Mine", "")
        dao.upsert(NoteEntity("shared", "Shared", "", 100L, synced = true))
        repository.deleteNote("id1")
        remote.notes["user"] = mutableMapOf(
            "shared" to RemoteNote("shared", "Shared", "", 900L, deleted = true),
        )

        repository.sync()

        assertTrue(dao.notes.value.isEmpty())
        assertTrue(remote.notes.getValue("user").getValue("id1").deleted)
    }

    @Test
    fun syncReportsFailureAndKeepsLocalChanges() = runTest {
        auth.signInAs("user")
        repository.saveNote(null, "Local", "")
        val error = IOException("offline")
        remote.failure = error

        val result = repository.sync()

        assertEquals(SyncResult.Failure(error), result)
        assertFalse(dao.notes.entities.single().synced)
    }
}
