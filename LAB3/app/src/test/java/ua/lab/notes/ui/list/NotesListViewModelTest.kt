package ua.lab.notes.ui.list

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import ua.lab.notes.MainDispatcherRule
import ua.lab.notes.data.NotesRepository
import ua.lab.notes.fakes.FakeAuth
import ua.lab.notes.fakes.FakeClock
import ua.lab.notes.fakes.FakeNoteDao
import ua.lab.notes.fakes.FakeRemote

class NotesListViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val auth = FakeAuth()
    private val remote = FakeRemote()
    private var nextId = 0
    private val repository =
        NotesRepository(FakeNoteDao(), remote, auth, FakeClock()) { "id${++nextId}" }

    @Test
    fun filtersNotesByQueryIgnoringCase() = runTest {
        repository.saveNote(null, "Shopping list", "milk")
        repository.saveNote(null, "Ideas", "Buy MILK chocolate")
        repository.saveNote(null, "Travel", "Lviv")
        val viewModel = NotesListViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onQueryChange(" milk ")

        val state = viewModel.uiState.first { it.query == " milk " }
        assertEquals(listOf("Ideas", "Shopping list"), state.notes.map { it.title }.sorted())
        assertEquals(3, state.totalCount)
        assertEquals(3, state.pendingChanges)
    }

    @Test
    fun manualSyncShowsResultUntilMessageIsShown() = runTest {
        auth.signInAs("user")
        repository.saveNote(null, "Note", "")
        val viewModel = NotesListViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.sync()

        val synced = viewModel.uiState.first { it.message != null }
        assertEquals(SyncMessage.Done(uploaded = 0, downloaded = 0), synced.message)
        assertFalse(synced.isSyncing)
        viewModel.messageShown()
        assertNull(viewModel.uiState.first { it.message == null }.message)
    }

    @Test
    fun syncWithoutAccountAsksToSignIn() = runTest {
        val viewModel = NotesListViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.sync()

        assertEquals(
            SyncMessage.NotSignedIn,
            viewModel.uiState.first { it.message != null }.message,
        )
    }

    @Test
    fun failedSyncIsReported() = runTest {
        auth.signInAs("user")
        remote.failure = IllegalStateException("offline")
        val viewModel = NotesListViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.sync()

        assertEquals(SyncMessage.Failed, viewModel.uiState.first { it.message != null }.message)
    }
}
