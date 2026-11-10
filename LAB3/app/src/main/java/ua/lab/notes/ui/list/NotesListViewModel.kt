package ua.lab.notes.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.lab.notes.data.Note
import ua.lab.notes.data.NotesRepository
import ua.lab.notes.data.sync.SyncResult
import ua.lab.notes.ui.appContainer

sealed interface SyncMessage {
    data class Done(val uploaded: Int, val downloaded: Int) : SyncMessage
    data object NotSignedIn : SyncMessage
    data object Unavailable : SyncMessage
    data object Failed : SyncMessage
}

data class NotesListUiState(
    val notes: List<Note> = emptyList(),
    val totalCount: Int = 0,
    val query: String = "",
    val pendingChanges: Int = 0,
    val isSyncing: Boolean = false,
    val message: SyncMessage? = null,
)

class NotesListViewModel(private val repository: NotesRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val syncing = MutableStateFlow(false)
    private val message = MutableStateFlow<SyncMessage?>(null)

    val uiState: StateFlow<NotesListUiState> = combine(
        repository.observeNotes(),
        repository.observePendingChanges(),
        query,
        syncing,
        message,
    ) { notes, pending, query, syncing, message ->
        NotesListUiState(
            notes = notes.filter { it.matches(query) },
            totalCount = notes.size,
            query = query,
            pendingChanges = pending,
            isSyncing = syncing,
            message = message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesListUiState())

    init {
        launchSync(showResult = false)
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun sync() = launchSync(showResult = true)

    fun messageShown() {
        message.value = null
    }

    private fun launchSync(showResult: Boolean) {
        if (syncing.value) return
        syncing.value = true
        viewModelScope.launch {
            val result = repository.sync()
            syncing.value = false
            if (showResult) message.value = result.toMessage()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { NotesListViewModel(appContainer().notesRepository) }
        }
    }
}

private fun Note.matches(query: String): Boolean =
    query.isBlank() ||
        title.contains(query.trim(), ignoreCase = true) ||
        content.contains(query.trim(), ignoreCase = true)

private fun SyncResult.toMessage(): SyncMessage = when (this) {
    is SyncResult.Success -> SyncMessage.Done(uploaded, downloaded)
    SyncResult.NotSignedIn -> SyncMessage.NotSignedIn
    SyncResult.Unavailable -> SyncMessage.Unavailable
    is SyncResult.Failure -> SyncMessage.Failed
}
