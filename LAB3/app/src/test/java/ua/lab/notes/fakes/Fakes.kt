package ua.lab.notes.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import ua.lab.notes.data.auth.AuthError
import ua.lab.notes.data.auth.AuthException
import ua.lab.notes.data.auth.AuthService
import ua.lab.notes.data.auth.UserAccount
import ua.lab.notes.data.local.NoteDao
import ua.lab.notes.data.local.NoteEntity
import ua.lab.notes.data.remote.NotesRemoteDataSource
import ua.lab.notes.data.remote.RemoteNote

class FakeNoteDao : NoteDao {
    val notes = MutableStateFlow<Map<String, NoteEntity>>(emptyMap())

    override fun observeNotes(): Flow<List<NoteEntity>> = notes.map { all ->
        all.values.filterNot { it.deleted }.sortedByDescending { it.updatedAt }
    }

    override fun observePendingCount(): Flow<Int> =
        notes.map { all -> all.values.count { !it.synced } }

    override suspend fun findById(id: String) = notes.value[id]?.takeUnless { it.deleted }

    override suspend fun getAll() = notes.value.values.toList()

    override suspend fun upsert(note: NoteEntity) {
        notes.value = notes.value + (note.id to note)
    }

    override suspend fun markSynced(id: String, updatedAt: Long) {
        val note = notes.value[id] ?: return
        if (note.updatedAt == updatedAt) upsert(note.copy(synced = true))
    }

    override suspend fun deleteById(id: String) {
        notes.value = notes.value - id
    }
}

class FakeRemote : NotesRemoteDataSource {
    val notes = mutableMapOf<String, MutableMap<String, RemoteNote>>()
    var failure: Exception? = null

    override suspend fun fetchAll(userId: String): List<RemoteNote> {
        failure?.let { throw it }
        return notes[userId].orEmpty().values.toList()
    }

    override suspend fun upload(userId: String, notes: List<RemoteNote>) {
        val userNotes = this.notes.getOrPut(userId) { mutableMapOf() }
        notes.forEach { userNotes[it.id] = it }
    }
}

class FakeAuth(override val isAvailable: Boolean = true) : AuthService {
    override val currentUser = MutableStateFlow<UserAccount?>(null)
    val registered = mutableMapOf<String, String>()
    var error: AuthError? = null

    override suspend fun signIn(email: String, password: String) {
        error?.let { throw AuthException(it) }
        if (registered[email] != password) throw AuthException(AuthError.INVALID_CREDENTIALS)
        currentUser.value = UserAccount("uid-$email", email)
    }

    override suspend fun register(email: String, password: String) {
        error?.let { throw AuthException(it) }
        registered[email] = password
        currentUser.value = UserAccount("uid-$email", email)
    }

    override fun signOut() {
        currentUser.value = null
    }

    fun signInAs(userId: String) {
        currentUser.value = UserAccount(userId, "$userId@example.com")
    }
}

class FakeClock(var time: Long = 1_000L) : ua.lab.notes.data.Clock {
    override fun now() = time
}

val StateFlow<Map<String, NoteEntity>>.entities: List<NoteEntity>
    get() = value.values.sortedBy { it.id }
