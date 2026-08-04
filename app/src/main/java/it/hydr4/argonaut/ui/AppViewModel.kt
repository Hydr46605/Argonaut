package it.hydr4.argonaut.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.hydr4.argonaut.data.AuthRepository
import it.hydr4.argonaut.data.SessionState
import it.hydr4.argonaut.data.SettingsRepository
import it.hydr4.argonaut.data.UserPreferences
import it.hydr4.argonaut.widget.WidgetSyncScheduler
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * App-shell view model: restores the persisted session once, surfaces the
 * session + preferences flows that drive the theme and navigation, and keeps
 * the widget sync schedule in sync with the session and the user's cadence.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val widgetSyncScheduler: WidgetSyncScheduler,
) : ViewModel() {

    val session: StateFlow<SessionState> = authRepository.session
    val preferences: StateFlow<UserPreferences> = settingsRepository.preferences

    init {
        viewModelScope.launch {
            authRepository.restoreSession()
        }
        viewModelScope.launch {
            authRepository.session.collect { current ->
                if (current is SessionState.Authenticated) {
                    widgetSyncScheduler.schedule(settingsRepository.preferences.value.widgetRefreshMinutes)
                } else {
                    widgetSyncScheduler.cancel()
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.preferences.collect { prefs ->
                if (authRepository.session.value is SessionState.Authenticated) {
                    widgetSyncScheduler.schedule(prefs.widgetRefreshMinutes)
                }
            }
        }
    }
}
