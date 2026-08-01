package it.hydr4.argonaut.data

import kotlinx.coroutines.flow.StateFlow

/** Dark mode strategy, mirroring the classic triad of overrides. */
enum class DarkModePreference {
    SYSTEM,
    LIGHT,
    DARK,
}

/** Immutable snapshot of every user-tunable preference. */
data class UserPreferences(
    val dynamicColor: Boolean = true,
    val darkMode: DarkModePreference = DarkModePreference.SYSTEM,
    val widgetRefreshMinutes: Int = DEFAULT_WIDGET_REFRESH_MINUTES,
    val showStudentName: Boolean = true,
    val lastSchoolCode: String = "",
) {
    companion object {
        const val DEFAULT_WIDGET_REFRESH_MINUTES = 30
        const val MIN_WIDGET_REFRESH_MINUTES = 15
    }
}

/**
 * User-tunable preferences: theme behavior and widget refresh cadence.
 * Exposed as a hot [StateFlow] so the theme and the scheduler react instantly.
 */
interface SettingsRepository {

    val preferences: StateFlow<UserPreferences>

    fun setDynamicColor(enabled: Boolean)

    fun setDarkMode(mode: DarkModePreference)

    fun setWidgetRefreshMinutes(minutes: Int)

    fun setShowStudentName(enabled: Boolean)

    fun setLastSchoolCode(code: String)
}
