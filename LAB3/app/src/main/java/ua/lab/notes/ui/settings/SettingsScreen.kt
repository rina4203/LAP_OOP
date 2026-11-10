package ua.lab.notes.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.lab.notes.R
import ua.lab.notes.data.auth.AuthError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val account = state.account

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.language_title), style = MaterialTheme.typography.titleMedium)
            AppLanguage.entries.forEach { language ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = language == state.language,
                            onClick = { viewModel.selectLanguage(language) },
                            role = Role.RadioButton,
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = language == state.language, onClick = null)
                    Text(languageName(language), modifier = Modifier.padding(start = 8.dp))
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text(stringResource(R.string.account_title), style = MaterialTheme.typography.titleMedium)
            when {
                !state.syncAvailable -> Text(stringResource(R.string.account_unavailable))
                account != null -> {
                    Text(stringResource(R.string.account_signed_in_as, account.email.orEmpty()))
                    OutlinedButton(onClick = viewModel::signOut) {
                        Text(stringResource(R.string.action_sign_out))
                    }
                }
                else -> SignInForm(state, viewModel)
            }
        }
    }
}

@Composable
private fun SignInForm(state: SettingsUiState, viewModel: SettingsViewModel) {
    OutlinedTextField(
        value = state.email,
        onValueChange = viewModel::onEmailChange,
        label = { Text(stringResource(R.string.email_hint)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = state.password,
        onValueChange = viewModel::onPasswordChange,
        label = { Text(stringResource(R.string.password_hint)) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
    )
    state.error?.let { error ->
        Text(errorText(error), color = MaterialTheme.colorScheme.error)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = viewModel::signIn, enabled = !state.isBusy) {
            Text(stringResource(R.string.action_sign_in))
        }
        OutlinedButton(onClick = viewModel::register, enabled = !state.isBusy) {
            Text(stringResource(R.string.action_register))
        }
        if (state.isBusy) {
            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
        }
    }
}

@Composable
private fun languageName(language: AppLanguage): String = when (language) {
    AppLanguage.SYSTEM -> stringResource(R.string.language_system)
    AppLanguage.ENGLISH -> stringResource(R.string.language_english)
    AppLanguage.UKRAINIAN -> stringResource(R.string.language_ukrainian)
}

@Composable
private fun errorText(error: SettingsError): String = stringResource(
    when (error) {
        SettingsError.EmptyFields -> R.string.error_empty_fields
        is SettingsError.Auth -> when (error.error) {
            AuthError.INVALID_CREDENTIALS -> R.string.error_invalid_credentials
            AuthError.INVALID_EMAIL -> R.string.error_invalid_email
            AuthError.EMAIL_IN_USE -> R.string.error_email_in_use
            AuthError.WEAK_PASSWORD -> R.string.error_weak_password
            AuthError.NETWORK -> R.string.error_network
            AuthError.UNKNOWN -> R.string.error_unknown
        }
    },
)
