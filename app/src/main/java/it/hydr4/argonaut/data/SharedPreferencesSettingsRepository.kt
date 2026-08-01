package it.hydr4.argonaut.data

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists preferences in SharedPreferences and exposes them as a hot
 * [StateFlow] so the theme and the widget scheduler react instantly to changes.
 */
@Singleton
class SharedPreferencesSettingsRepository @Inject constructor(
    @ApplicationContext context: Context,
) : SettingsRepository {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mutablePrefs = MutableStateFlow(read())

    override val preferences: StateFlow<UserPreferences> = mutablePrefs.asStateFlow()

    override fun setDynamicColor(enabled: Boolean) = update { it.copy(dynamicColor = enabled) }

    override fun setDarkMode(mode: DarkModePreference) = update { it.copy(darkMode = mode) }

    override fun setWidgetRefreshMinutes(minutes: Int) {
        val clamped = minutes.coerceAtLeast(UserPreferences.MIN_WIDGET_REFRESH_MINUTES)
        update { it.copy(widgetRefreshMinutes = clamped) }
    }

    override fun setShowStudentName(enabled: Boolean) = update { it.copy(showStudentName = enabled) }

    override fun setLastSchoolCode(code: String) = update { it.copy(lastSchoolCode = code) }

    private fun update(transform: (UserPreferences) -> UserPreferences) {
        val next = transform(mutablePrefs.value)
        prefs.edit {
            putBoolean(KEY_DYNAMIC_COLOR, next.dynamicColor)
            putString(KEY_DARK_MODE, next.darkMode.name)
            putInt(KEY_WIDGET_REFRESH_MINUTES, next.widgetRefreshMinutes)
            putBoolean(KEY_SHOW_STUDENT_NAME, next.showStudentName)
            putString(KEY_LAST_SCHOOL_CODE, next.lastSchoolCode)
        }
        mutablePrefs.value = next
    }

    private fun read(): UserPreferences = UserPreferences(
        dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true),
        darkMode = runCatching {
            DarkModePreference.valueOf(prefs.getString(KEY_DARK_MODE, DarkModePreference.SYSTEM.name)!!)
        }.getOrDefault(DarkModePreference.SYSTEM),
        widgetRefreshMinutes = prefs.getInt(KEY_WIDGET_REFRESH_MINUTES, UserPreferences.DEFAULT_WIDGET_REFRESH_MINUTES),
        showStudentName = prefs.getBoolean(KEY_SHOW_STUDENT_NAME, true),
        lastSchoolCode = prefs.getString(KEY_LAST_SCHOOL_CODE, "") ?: "",
    )

    private companion object {
        const val PREFS_NAME = "argonaut_preferences"
        const val KEY_DYNAMIC_COLOR = "dynamic_color"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_WIDGET_REFRESH_MINUTES = "widget_refresh_minutes"
        const val KEY_SHOW_STUDENT_NAME = "widget_show_student_name"
        const val KEY_LAST_SCHOOL_CODE = "last_school_code"
    }
}
