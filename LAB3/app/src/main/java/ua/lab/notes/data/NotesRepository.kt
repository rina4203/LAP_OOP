package ua.lab.notes.data

import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import ua.lab.notes.data.auth.AuthService
import ua.lab.notes.data.local.NoteDao
import ua.lab.notes.data.local.NoteEntity
import ua.lab.notes.data.local.toEntity
import ua.lab.notes.data.local.toNote
import ua.lab.notes.data.local.toRemote
import ua.lab.notes.data.remote.NotesRemoteDataSource
import ua.lab.notes.data.sync.SyncPlanner
import ua.lab.notes.data.sync.SyncResult

class NotesRepository(
    private val dao: NoteDao,
    private val remote: NotesRemoteDataSource?,
    private val auth: AuthService,
    private val clock: Clock = Clock.SYSTEM,
    private val newId: () -> String = { UUID.randomUUID().toString() },
) {
    private val syncLock = Mutex()

    fun observeNotes(): Flow<List<Note>> =
        dao.observeNotes().map { notes -> notes.map(NoteEntity::toNote) }

    fun observePendingChanges(): Flow<Int> = dao.observePendingCount()

    suspend fun getNote(id: String): Note? = dao.findById(id)?.toNote()

    suspend fun saveNote(id: String?, title: String, content: String): Note {
        val note = NoteEntity(id ?: newId(), title.trim(), content, clock.now())
        dao.upsert(note)
        Timber.d("Note %s saved", note.id)
        return note.toNote()
    }

    suspend fun deleteNote(id: String) {
        val note = dao.findById(id) ?: return
        dao.upsert(note.copy(deleted = true, synced = false, updatedAt = clock.now()))
        Timber.d("Note %s marked as deleted", id)
    }

    suspend fun sync(): SyncResult = syncLock.withLock { syncLocked() }

    private suspend fun syncLocked(): SyncResult {
        if (remote == null || !auth.isAvailable) {
            return SyncResult.Unavailable
        }
        val userId = auth.currentUser.value?.id ?: return SyncResult.NotSignedIn
        Timber.i("Sync started")
        return try {
            val plan = SyncPlanner.plan(dao.getAll(), remote.fetchAll(userId))
            remote.upload(userId, plan.upload.map(NoteEntity::toRemote))
            plan.upload.forEach { note ->
                if (note.deleted) dao.deleteById(note.id)
                else dao.markSynced(note.id, note.updatedAt)
            }
            plan.download.forEach { note ->
                if (note.deleted) dao.deleteById(note.id) else dao.upsert(note.toEntity())
            }
            Timber.i(
                "Sync finished: uploaded %d, downloaded %d",
                plan.upload.size, plan.download.size,
            )
            SyncResult.Success(plan.upload.size, plan.download.size)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Sync failed")
            SyncResult.Failure(e)
        }
    }
}
