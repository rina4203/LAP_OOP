package ua.lab.notes.data.remote

interface NotesRemoteDataSource {

    suspend fun fetchAll(userId: String): List<RemoteNote>

    suspend fun upload(userId: String, notes: List<RemoteNote>)
}
