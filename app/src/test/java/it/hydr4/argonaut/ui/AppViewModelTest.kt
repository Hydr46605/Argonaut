package it.hydr4.argonaut.ui

import it.hydr4.argonaut.data.SessionState
import it.hydr4.argonaut.data.UserPreferences
import it.hydr4.argonaut.testing.FakeAuthRepository
import it.hydr4.argonaut.testing.FakeSettingsRepository
import it.hydr4.argonaut.testing.FakeWidgetSyncScheduler
import it.hydr4.argonaut.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AppViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `authenticated session schedules the widget sync at the configured cadence`() {
        val settings = FakeSettingsRepository(UserPreferences(widgetRefreshMinutes = 60))
        val scheduler = FakeWidgetSyncScheduler()
        val auth = FakeAuthRepository(initialSession = SessionState.Restoring)
        AppViewModel(auth, settings, scheduler)

        auth.setSession(SessionState.Authenticated("Mario Rossi", "3ªA", "IIS Esempio"))

        assertEquals(60, scheduler.lastScheduledMinutes)
    }

    @Test
    fun `anonymous session cancels the widget sync`() {
        val scheduler = FakeWidgetSyncScheduler()
        val auth = FakeAuthRepository(initialSession = SessionState.Restoring)
        AppViewModel(auth, FakeSettingsRepository(), scheduler)

        auth.setSession(SessionState.Anonymous)

        assertEquals(1, scheduler.cancelCalls)
    }

    @Test
    fun `changing the cadence reschedules while authenticated`() {
        val settings = FakeSettingsRepository(UserPreferences(widgetRefreshMinutes = 30))
        val scheduler = FakeWidgetSyncScheduler()
        val auth = FakeAuthRepository(initialSession = SessionState.Restoring)
        AppViewModel(auth, settings, scheduler)
        auth.setSession(SessionState.Authenticated("Mario Rossi", "3ªA", "IIS Esempio"))

        settings.setWidgetRefreshMinutes(180)

        assertEquals(180, scheduler.lastScheduledMinutes)
    }
}
