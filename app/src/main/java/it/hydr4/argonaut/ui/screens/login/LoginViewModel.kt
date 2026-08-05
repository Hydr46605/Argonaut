package it.hydr4.argonaut.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.hydr4.argonaut.data.AuthRepository
import it.hydr4.argonaut.data.LoginFailure
import it.hydr4.argonaut.data.SettingsRepository
import it.hydr4.argonaut.domain.CredentialValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Whole login screen state; form fields live here as immutable snapshots. */
sealed interface LoginUiState {
    data object Loading : LoginUiState

    data class Ready(
        val schoolCode: String = "",
        val username: String = "",
        val password: String = "",
        val schoolCodeError: CredentialValidator.FieldError? = null,
        val usernameError: CredentialValidator.FieldError? = null,
        val passwordError: CredentialValidator.FieldError? = null,
        val isSubmitting: Boolean = false,
        val failure: LoginFailure? = null,
    ) : LoginUiState
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val validator: CredentialValidator,
) : ViewModel() {

    private val mutableState = MutableStateFlow<LoginUiState>(LoginUiState.Loading)
    val uiState: StateFlow<LoginUiState> = mutableState.asStateFlow()

    init {
        mutableState.value = LoginUiState.Ready(
            schoolCode = settingsRepository.preferences.value.lastSchoolCode,
        )
    }

    fun onSchoolCodeChange(value: String) = updateReady { it.copy(schoolCode = value, schoolCodeError = null, failure = null) }

    fun onUsernameChange(value: String) = updateReady { it.copy(username = value, usernameError = null, failure = null) }

    fun onPasswordChange(value: String) = updateReady { it.copy(password = value, passwordError = null, failure = null) }

    fun submit() {
        val current = (mutableState.value as? LoginUiState.Ready) ?: return
        if (current.isSubmitting) return

        val result = validator.validate(current.schoolCode, current.username, current.password)
        if (!result.isValid) {
            mutableState.value = current.copy(
                schoolCodeError = result.schoolCodeError,
                usernameError = result.usernameError,
                passwordError = result.passwordError,
            )
        } else {
            mutableState.value = current.copy(isSubmitting = true, failure = null)
            viewModelScope.launch { performLogin(current) }
        }
    }

    private suspend fun performLogin(current: LoginUiState.Ready) {
        val failure = authRepository.login(
            schoolCode = current.schoolCode.trim(),
            username = current.username.trim(),
            password = current.password,
        )
        val latest = mutableState.value as? LoginUiState.Ready ?: return
        mutableState.value = if (failure == null) {
            // Session flow flipped to Authenticated; the nav graph rebuilds.
            latest.copy(isSubmitting = false)
        } else {
            latest.copy(
                isSubmitting = false,
                password = "",
                passwordError = null,
                failure = failure,
            )
        }
    }

    private fun updateReady(transform: (LoginUiState.Ready) -> LoginUiState.Ready) {
        val current = mutableState.value as? LoginUiState.Ready ?: return
        mutableState.value = transform(current)
    }
}
