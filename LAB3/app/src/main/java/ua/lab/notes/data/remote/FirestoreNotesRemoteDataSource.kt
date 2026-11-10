package ua.lab.notes.data.remote

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

class FirestoreNotesRemoteDataSource(
    private val firestore: FirebaseFirestore,
) : NotesRemoteDataSource {

    // Source.SERVER: без мережі синхронізація одразу завершується помилкою,
    // а не працює зі застарілим кешем Firestore
    override suspend fun fetchAll(userId: String): List<RemoteNote> =
        notes(userId).get(Source.SERVER).await().documents.map { document ->
            RemoteNote(
                id = document.id,
                title = document.getString(TITLE).orEmpty(),
                content = document.getString(CONTENT).orEmpty(),
                updatedAt = document.getLong(UPDATED_AT) ?: 0L,
                deleted = document.getBoolean(DELETED) ?: false,
            )
        }

    override suspend fun upload(userId: String, notes: List<RemoteNote>) {
        notes.chunked(MAX_BATCH_SIZE).forEach { chunk ->
            firestore.runBatch { batch ->
                chunk.forEach { note ->
                    batch.set(
                        notes(userId).document(note.id),
                        mapOf(
                            TITLE to note.title,
                            CONTENT to note.content,
                            UPDATED_AT to note.updatedAt,
                            DELETED to note.deleted,
                        ),
                    )
                }
            }.await()
        }
    }

    private fun notes(userId: String): CollectionReference =
        firestore.collection(USERS).document(userId).collection(NOTES)

    private companion object {
        const val USERS = "users"
        const val NOTES = "notes"
        const val TITLE = "title"
        const val CONTENT = "content"
        const val UPDATED_AT = "updatedAt"
        const val DELETED = "deleted"
        const val MAX_BATCH_SIZE = 500
    }
}
