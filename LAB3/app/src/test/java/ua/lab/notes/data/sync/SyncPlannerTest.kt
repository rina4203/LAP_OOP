package ua.lab.notes.data.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.lab.notes.data.local.NoteEntity
import ua.lab.notes.data.remote.RemoteNote

class SyncPlannerTest {

    @Test
    fun uploadsLocalChangesMissingInCloud() {
        val local = note("a", updatedAt = 10)

        val plan = SyncPlanner.plan(listOf(local), emptyList())

        assertEquals(listOf(local), plan.upload)
        assertTrue(plan.download.isEmpty())
    }

    @Test
    fun downloadsCloudNotesMissingLocally() {
        val cloud = remote("a", updatedAt = 10)

        val plan = SyncPlanner.plan(emptyList(), listOf(cloud))

        assertEquals(listOf(cloud), plan.download)
        assertTrue(plan.upload.isEmpty())
    }

    @Test
    fun ignoresCloudDeletionsOfNotesNeverSeenLocally() {
        val plan = SyncPlanner.plan(emptyList(), listOf(remote("a", 10, deleted = true)))

        assertTrue(plan.download.isEmpty())
    }

    @Test
    fun newerLocalChangeWins() {
        val local = note("a", updatedAt = 20)

        val plan = SyncPlanner.plan(listOf(local), listOf(remote("a", updatedAt = 10)))

        assertEquals(listOf(local), plan.upload)
        assertTrue(plan.download.isEmpty())
    }

    @Test
    fun newerCloudChangeWins() {
        val cloud = remote("a", updatedAt = 20)

        val plan = SyncPlanner.plan(listOf(note("a", updatedAt = 10)), listOf(cloud))

        assertEquals(listOf(cloud), plan.download)
        assertTrue(plan.upload.isEmpty())
    }

    @Test
    fun syncedNotesWithoutChangesAreLeftAlone() {
        val plan = SyncPlanner.plan(
            listOf(note("a", updatedAt = 10, synced = true)),
            listOf(remote("a", updatedAt = 10)),
        )

        assertTrue(plan.upload.isEmpty())
        assertTrue(plan.download.isEmpty())
    }

    @Test
    fun localDeletionIsUploadedAsTombstone() {
        val deleted = note("a", updatedAt = 20, deleted = true)

        val plan = SyncPlanner.plan(listOf(deleted), listOf(remote("a", updatedAt = 10)))

        assertEquals(listOf(deleted), plan.upload)
    }

    @Test
    fun cloudDeletionIsDownloadedForKnownNote() {
        val tombstone = remote("a", updatedAt = 20, deleted = true)

        val plan = SyncPlanner.plan(listOf(note("a", 10, synced = true)), listOf(tombstone))

        assertEquals(listOf(tombstone), plan.download)
    }

    private fun note(
        id: String,
        updatedAt: Long,
        synced: Boolean = false,
        deleted: Boolean = false,
    ) = NoteEntity(id, "title $id", "content", updatedAt, deleted, synced)

    private fun remote(id: String, updatedAt: Long, deleted: Boolean = false) =
        RemoteNote(id, "cloud $id", "content", updatedAt, deleted)
}
