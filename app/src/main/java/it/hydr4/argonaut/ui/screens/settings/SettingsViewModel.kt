package it.hydr4.argonaut.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.hydr4.argonaut.data.AuthRepository
import it.hydr4.argonaut.data.DarkModePreference
import it.hydr4.argonaut.data.SessionState
import it.hydr4.argonaut.data.SettingsRepository
import it.hydr4.argonaut.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Whole settings screen state. */
data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val loggedInAs: String? = null,
    val isLoggingOut: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val loggingOut = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.preferences,
        authRepository.session,
        loggingOut,
    ) { preferences, session, isLoggingOut ->
        SettingsUiState(
            preferences = preferences,
            loggedInAs = (session as? SessionState.Authenticated)?.studentName,
            isLoggingOut = isLoggingOut,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState(),
    )

    fun setDynamicColor(enabled: Boolean) = settingsRepository.setDynamicColor(enabled)

    fun setDarkMode(mode: DarkModePreference) = settingsRepository.setDarkMode(mode)

    fun setWidgetRefreshMinutes(minutes: Int) = settingsRepository.setWidgetRefreshMinutes(minutes)

    fun setShowStudentName(enabled: Boolean) = settingsRepository.setShowStudentName(enabled)

    fun logout() {
        viewModelScope.launch {
            loggingOut.value = true
            authRepository.logout()
            // Session flips to Anonymous; the nav graph rebuilds onto login.
        }
    }
}
