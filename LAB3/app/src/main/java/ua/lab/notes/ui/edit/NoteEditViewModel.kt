package ua.lab.notes.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.lab.notes.data.Note
import ua.lab.notes.data.NotesRepository
import ua.lab.notes.ui.appContainer

data class NoteEditUiState(
    val title: String = "",
    val content: String = "",
    val isNew: Boolean = true,
)

class NoteEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: NotesRepository,
) : ViewModel() {

    private val noteId: String? = savedStateHandle[NOTE_ID_ARG]
    private var original: Note? = null
    private var finished = false

    private val state = MutableStateFlow(NoteEditUiState(isNew = noteId == null))
    val uiState: StateFlow<NoteEditUiState> = state.asStateFlow()

    init {
        if (noteId != null) {
            viewModelScope.launch {
                val note = repository.getNote(noteId)
                original = note
                state.value = NoteEditUiState(
                    title = note?.title.orEmpty(),
                    content = note?.content.orEmpty(),
                    isNew = note == null,
                )
            }
        }
    }

    fun onTitleChange(value: String) = state.update { it.copy(title = value) }

    fun onContentChange(value: String) = state.update { it.copy(content = value) }

    fun save(onSaved: () -> Unit) = finish(onSaved) {
        val current = state.value
        if (hasChanges(current)) {
            repository.saveNote(original?.id, current.title, current.content)
        }
    }

    fun delete(onDeleted: () -> Unit) = finish(onDeleted) {
        original?.let { repository.deleteNote(it.id) }
    }

    private fun hasChanges(current: NoteEditUiState): Boolean {
        val note = original ?: return current.title.isNotBlank() || current.content.isNotBlank()
        return note.title != current.title.trim() || note.content != current.content
    }

    // Захист від повторного натискання "Назад", яке закрило б і список нотаток
    private fun finish(onFinished: () -> Unit, action: suspend () -> Unit) {
        if (finished) return
        finished = true
        viewModelScope.launch {
            action()
            onFinished()
        }
    }

    companion object {
        const val NOTE_ID_ARG = "noteId"

        val Factory = viewModelFactory {
            initializer {
                NoteEditViewModel(createSavedStateHandle(), appContainer().notesRepository)
            }
        }
    }
}
