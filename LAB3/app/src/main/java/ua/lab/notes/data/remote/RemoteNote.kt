package ua.lab.notes.data.remote

data class RemoteNote(
    val id: String,
    val title: String,
    val content: String,
    val updatedAt: Long,
    val deleted: Boolean,
)
