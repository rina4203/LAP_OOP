package ua.lab.notes.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import ua.lab.notes.data.Note
import ua.lab.notes.data.remote.RemoteNote

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val updatedAt: Long,
    val deleted: Boolean = false,
    val synced: Boolean = false,
)

fun NoteEntity.toNote() = Note(id, title, content, updatedAt)

fun NoteEntity.toRemote() = RemoteNote(id, title, content, updatedAt, deleted)

fun RemoteNote.toEntity() =
    NoteEntity(id, title, content, updatedAt, deleted, synced = true)
