package it.hydr4.argonaut.testing

import it.hydr4.argonaut.data.AuthRepository
import it.hydr4.argonaut.data.DarkModePreference
import it.hydr4.argonaut.data.DashboardRepository
import it.hydr4.argonaut.data.LoginFailure
import it.hydr4.argonaut.data.RefreshFailure
import it.hydr4.argonaut.data.RefreshResult
import it.hydr4.argonaut.data.SessionState
import it.hydr4.argonaut.data.SettingsRepository
import it.hydr4.argonaut.data.UserPreferences
import it.hydr4.argonaut.data.model.DashboardSummary
import it.hydr4.argonaut.data.model.ScheduleSlotItem
import it.hydr4.argonaut.widget.WidgetSyncScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** In-memory [AuthRepository] driven by a configurable [loginFailure]. */
class FakeAuthRepository(
    var loginFailure: LoginFailure? = null,
    initialSession: SessionState = SessionState.Anonymous,
) : AuthRepository {

    private val mutableSession = MutableStateFlow(initialSession)

    override val session: StateFlow<SessionState> = mutableSession.asStateFlow()

    var loginCalls = 0
        private set
    var lastLogin: Triple<String, String, String>? = null
        private set
    var logoutCalls = 0
        private set

    override suspend fun restoreSession() {
        mutableSession.value = SessionState.Anonymous
    }

    override suspend fun login(schoolCode: String, username: String, password: String): LoginFailure? {
        loginCalls += 1
        lastLogin = Triple(schoolCode, username, password)
        return loginFailure?.also {
            mutableSession.value = SessionState.Anonymous
        } ?: run {
            mutableSession.value = SessionState.Authenticated("Mario Rossi", "3ªA", "IIS Esempio")
            null
        }
    }

    override fun invalidateSession() {
        mutableSession.value = SessionState.Anonymous
    }

    override suspend fun logout() {
        logoutCalls += 1
        mutableSession.value = SessionState.Anonymous
    }

    fun setSession(state: SessionState) {
        mutableSession.value = state
    }
}

/** In-memory [DashboardRepository] driven by configurable results. */
class FakeDashboardRepository(
    var refreshResult: RefreshResult = RefreshResult.Success(DashboardSummary(overallAverage = 8.4)),
    var cached: DashboardSummary? = null,
    var schedule: List<ScheduleSlotItem> = emptyList(),
) : DashboardRepository {

    var refreshCalls = 0
        private set

    override fun cachedSummary(): DashboardSummary? = cached

    override fun cachedScheduleItems(): List<ScheduleSlotItem> = schedule

    override suspend fun refresh(forceRefresh: Boolean): RefreshResult {
        refreshCalls += 1
        return refreshResult
    }

    override suspend fun todaySchedule(forceRefresh: Boolean): List<ScheduleSlotItem> = schedule
}

/** In-memory [SettingsRepository]. */
class FakeSettingsRepository(
    initial: UserPreferences = UserPreferences(),
) : SettingsRepository {

    private val mutablePrefs = MutableStateFlow(initial)

    override val preferences: StateFlow<UserPreferences> = mutablePrefs.asStateFlow()

    override fun setDynamicColor(enabled: Boolean) {
        mutablePrefs.value = mutablePrefs.value.copy(dynamicColor = enabled)
    }

    override fun setDarkMode(mode: DarkModePreference) {
        mutablePrefs.value = mutablePrefs.value.copy(darkMode = mode)
    }

    override fun setWidgetRefreshMinutes(minutes: Int) {
        mutablePrefs.value = mutablePrefs.value.copy(widgetRefreshMinutes = minutes)
    }

    override fun setShowStudentName(enabled: Boolean) {
        mutablePrefs.value = mutablePrefs.value.copy(showStudentName = enabled)
    }

    override fun setLastSchoolCode(code: String) {
        mutablePrefs.value = mutablePrefs.value.copy(lastSchoolCode = code)
    }
}

/** Records schedule/cancel calls. */
class FakeWidgetSyncScheduler : WidgetSyncScheduler {
    var lastScheduledMinutes: Int? = null
        private set
    var cancelCalls = 0
        private set

    override fun schedule(refreshMinutes: Int) {
        lastScheduledMinutes = refreshMinutes
    }

    override fun cancel() {
        cancelCalls += 1
    }
}

/** Default refresh-failure helper for tests. */
fun refreshFailure(reason: RefreshFailure): RefreshResult = RefreshResult.Failure(reason)
