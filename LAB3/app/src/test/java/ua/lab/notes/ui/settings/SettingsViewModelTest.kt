package ua.lab.notes.ui.settings

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ua.lab.notes.MainDispatcherRule
import ua.lab.notes.data.NotesRepository
import ua.lab.notes.data.auth.AuthError
import ua.lab.notes.fakes.FakeAuth
import ua.lab.notes.fakes.FakeClock
import ua.lab.notes.fakes.FakeNoteDao
import ua.lab.notes.fakes.FakeRemote

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val auth = FakeAuth()
    private val remote = FakeRemote()
    private val languages = FakeLanguageController(AppLanguage.UKRAINIAN)
    private val repository = NotesRepository(FakeNoteDao(), remote, auth, FakeClock())
    private val viewModel by lazy { SettingsViewModel(auth, repository, languages) }

    @Test
    fun startsWithCurrentLanguageAndAvailability() {
        assertEquals(AppLanguage.UKRAINIAN, viewModel.uiState.value.language)
        assertTrue(viewModel.uiState.value.syncAvailable)
    }

    @Test
    fun selectingLanguageAppliesIt() = runTest {
        observe()

        viewModel.selectLanguage(AppLanguage.ENGLISH)

        assertEquals(AppLanguage.ENGLISH, languages.language)
        viewModel.uiState.first { it.language == AppLanguage.ENGLISH }
    }

    @Test
    fun emptyFieldsAreReported() = runTest {
        observe()
        viewModel.onEmailChange("  ")

        viewModel.signIn()

        viewModel.uiState.first { it.error == SettingsError.EmptyFields }
    }

    @Test
    fun registeringSignsInAndSyncs() = runTest {
        observe()
        repository.saveNote(null, "Before sign in", "")
        viewModel.onEmailChange(" me@example.com ")
        viewModel.onPasswordChange("secret1")

        viewModel.register()

        val state = viewModel.uiState.first { it.account != null && it.password.isEmpty() }
        assertEquals("me@example.com", state.account?.email)
        assertFalse(state.isBusy)
        assertEquals(1, remote.notes.getValue("uid-me@example.com").size)
    }

    @Test
    fun authErrorIsShownAndEditingClearsIt() = runTest {
        observe()
        auth.error = AuthError.WEAK_PASSWORD
        viewModel.onEmailChange("me@example.com")
        viewModel.onPasswordChange("1")

        viewModel.register()

        viewModel.uiState.first { it.error == SettingsError.Auth(AuthError.WEAK_PASSWORD) }
        viewModel.onPasswordChange("12")
        assertNull(viewModel.uiState.first { it.password == "12" }.error)
    }

    @Test
    fun wrongPasswordIsReportedOnSignIn() = runTest {
        observe()
        auth.registered["me@example.com"] = "right"
        viewModel.onEmailChange("me@example.com")
        viewModel.onPasswordChange("wrong")

        viewModel.signIn()

        viewModel.uiState.first {
            it.error == SettingsError.Auth(AuthError.INVALID_CREDENTIALS) && !it.isBusy
        }
    }

    @Test
    fun signOutForgetsAccount() = runTest {
        observe()
        auth.signInAs("user")
        viewModel.uiState.first { it.account != null }

        viewModel.signOut()

        assertNull(viewModel.uiState.first { it.account == null }.account)
    }

    private fun TestScope.observe() {
        backgroundScope.launch { viewModel.uiState.collect {} }
    }

    private class FakeLanguageController(var language: AppLanguage) : LanguageController {
        override fun current() = language

        override fun apply(language: AppLanguage) {
            this.language = language
        }
    }
}
