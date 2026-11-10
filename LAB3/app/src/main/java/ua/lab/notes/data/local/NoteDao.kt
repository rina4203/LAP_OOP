package ua.lab.notes.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE deleted = 0 ORDER BY updatedAt DESC")
    fun observeNotes(): Flow<List<NoteEntity>>

    @Query("SELECT COUNT(*) FROM notes WHERE synced = 0")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM notes WHERE id = :id AND deleted = 0")
    suspend fun findById(id: String): NoteEntity?

    @Query("SELECT * FROM notes")
    suspend fun getAll(): List<NoteEntity>

    @Upsert
    suspend fun upsert(note: NoteEntity)

    // Позначає нотатку синхронізованою, лише якщо її не змінили під час синхронізації
    @Query("UPDATE notes SET synced = 1 WHERE id = :id AND updatedAt = :updatedAt")
    suspend fun markSynced(id: String, updatedAt: Long)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: String)
}
