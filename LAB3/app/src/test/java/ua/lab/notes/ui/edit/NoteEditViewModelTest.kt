package ua.lab.notes.ui.edit

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ua.lab.notes.MainDispatcherRule
import ua.lab.notes.data.NotesRepository
import ua.lab.notes.fakes.FakeAuth
import ua.lab.notes.fakes.FakeClock
import ua.lab.notes.fakes.FakeNoteDao
import ua.lab.notes.fakes.FakeRemote

class NoteEditViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val clock = FakeClock()
    private val repository =
        NotesRepository(FakeNoteDao(), FakeRemote(), FakeAuth(), clock) { "new" }

    @Test
    fun createsNewNoteOnSave() = runTest {
        val viewModel = viewModel(noteId = null)
        var closed = false

        viewModel.onTitleChange("Plan")
        viewModel.onContentChange("Write the report")
        viewModel.save { closed = true }

        assertTrue(closed)
        assertEquals("Write the report", repository.getNote("new")?.content)
    }

    @Test
    fun emptyNewNoteIsNotSaved() = runTest {
        var closed = false

        viewModel(noteId = null).save { closed = true }

        assertTrue(closed)
        assertTrue(repository.observeNotes().first().isEmpty())
    }

    @Test
    fun loadsAndUpdatesExistingNote() = runTest {
        repository.saveNote("n1", "Old", "text")
        val viewModel = viewModel(noteId = "n1")
        assertEquals("Old", viewModel.uiState.value.title)
        assertFalse(viewModel.uiState.value.isNew)
        clock.time = 5_000L

        viewModel.onTitleChange("New")
        viewModel.save {}

        assertEquals(5_000L, repository.getNote("n1")?.updatedAt)
        assertEquals("New", repository.getNote("n1")?.title)
    }

    @Test
    fun unchangedNoteKeepsItsTimestamp() = runTest {
        repository.saveNote("n1", "Same", "text")
        val viewModel = viewModel(noteId = "n1")
        clock.time = 9_000L

        viewModel.save {}

        assertEquals(1_000L, repository.getNote("n1")?.updatedAt)
    }

    @Test
    fun deletesNote() = runTest {
        repository.saveNote("n1", "Trash", "")
        var closed = false

        viewModel(noteId = "n1").delete { closed = true }

        assertTrue(closed)
        assertNull(repository.getNote("n1"))
    }

    @Test
    fun secondSaveIsIgnored() = runTest {
        val viewModel = viewModel(noteId = null)
        var closedTimes = 0
        viewModel.onTitleChange("Once")

        viewModel.save { closedTimes++ }
        viewModel.save { closedTimes++ }

        assertEquals(1, closedTimes)
    }

    private fun viewModel(noteId: String?) = NoteEditViewModel(
        SavedStateHandle(mapOf(NoteEditViewModel.NOTE_ID_ARG to noteId)),
        repository,
    )
}
