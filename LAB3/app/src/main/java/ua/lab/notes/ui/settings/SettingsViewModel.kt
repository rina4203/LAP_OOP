package ua.lab.notes.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.lab.notes.data.NotesRepository
import ua.lab.notes.data.auth.AuthError
import ua.lab.notes.data.auth.AuthException
import ua.lab.notes.data.auth.AuthService
import ua.lab.notes.data.auth.UserAccount
import ua.lab.notes.ui.appContainer

sealed interface SettingsError {
    data object EmptyFields : SettingsError
    data class Auth(val error: AuthError) : SettingsError
}

data class SettingsUiState(
    val language: AppLanguage = AppLanguage.SYSTEM,
    val syncAvailable: Boolean = false,
    val account: UserAccount? = null,
    val email: String = "",
    val password: String = "",
    val isBusy: Boolean = false,
    val error: SettingsError? = null,
)

class SettingsViewModel(
    private val auth: AuthService,
    private val repository: NotesRepository,
    private val languages: LanguageController,
) : ViewModel() {

    private val form = MutableStateFlow(
        SettingsUiState(language = languages.current(), syncAvailable = auth.isAvailable),
    )

    val uiState: StateFlow<SettingsUiState> =
        combine(form, auth.currentUser) { state, user -> state.copy(account = user) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), form.value)

    fun onEmailChange(value: String) = form.update { it.copy(email = value, error = null) }

    fun onPasswordChange(value: String) =
        form.update { it.copy(password = value, error = null) }

    fun signIn() = authenticate { email, password -> auth.signIn(email, password) }

    fun register() = authenticate { email, password -> auth.register(email, password) }

    fun signOut() = auth.signOut()

    fun selectLanguage(language: AppLanguage) {
        languages.apply(language)
        form.update { it.copy(language = language) }
    }

    private fun authenticate(action: suspend (String, String) -> Unit) {
        val current = form.value
        if (current.email.isBlank() || current.password.isEmpty()) {
            form.update { it.copy(error = SettingsError.EmptyFields) }
            return
        }
        form.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            try {
                action(current.email.trim(), current.password)
                form.update { it.copy(password = "", isBusy = false) }
                repository.sync()
            } catch (e: AuthException) {
                form.update { it.copy(isBusy = false, error = SettingsError.Auth(e.error)) }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                SettingsViewModel(
                    container.authService,
                    container.notesRepository,
                    AppCompatLanguageController(),
                )
            }
        }
    }
}
