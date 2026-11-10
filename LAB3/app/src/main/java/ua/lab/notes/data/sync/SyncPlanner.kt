package ua.lab.notes.data.sync

import ua.lab.notes.data.local.NoteEntity
import ua.lab.notes.data.remote.RemoteNote

data class SyncPlan(
    val upload: List<NoteEntity>,
    val download: List<RemoteNote>,
)

/**
 * Злиття за правилом "перемагає новіша зміна". Видалення передаються як
 * нотатки з прапорцем deleted, щоб інші пристрої теж видалили їх.
 */
object SyncPlanner {

    fun plan(local: List<NoteEntity>, remote: List<RemoteNote>): SyncPlan {
        val remoteById = remote.associateBy { it.id }
        val localById = local.associateBy { it.id }

        val upload = local.filter { note ->
            val cloud = remoteById[note.id]
            !note.synced && (cloud == null || note.updatedAt >= cloud.updatedAt)
        }
        val download = remote.filter { cloud ->
            val note = localById[cloud.id]
            if (note == null) !cloud.deleted else cloud.updatedAt > note.updatedAt
        }
        return SyncPlan(upload, download)
    }
}
