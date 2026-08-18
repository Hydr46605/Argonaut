package it.hydr4.argonaut.ui.screens.settings

import it.hydr4.argonaut.data.DarkModePreference
import it.hydr4.argonaut.data.SessionState
import it.hydr4.argonaut.testing.FakeAuthRepository
import it.hydr4.argonaut.testing.FakeSettingsRepository
import it.hydr4.argonaut.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `toggling dynamic color propagates to preferences`() {
        val settings = FakeSettingsRepository()
        val viewModel = SettingsViewModel(settings, FakeAuthRepository())

        viewModel.setDynamicColor(false)

        assertFalse(settings.preferences.value.dynamicColor)
    }

    @Test
    fun `dark mode override propagates`() {
        val settings = FakeSettingsRepository()
        val viewModel = SettingsViewModel(settings, FakeAuthRepository())

        viewModel.setDarkMode(DarkModePreference.DARK)

        assertEquals(DarkModePreference.DARK, settings.preferences.value.darkMode)
    }

    @Test
    fun `widget refresh minutes propagate`() {
        val settings = FakeSettingsRepository()
        val viewModel = SettingsViewModel(settings, FakeAuthRepository())

        viewModel.setWidgetRefreshMinutes(60)

        assertEquals(60, settings.preferences.value.widgetRefreshMinutes)
    }

    @Test
    fun `logged-in name is surfaced in the state`() {
        val auth = FakeAuthRepository(initialSession = SessionState.Authenticated("Mario Rossi", "3ªA", "IIS Esempio"))
        val viewModel = SettingsViewModel(FakeSettingsRepository(), auth)

        assertEquals("Mario Rossi", viewModel.uiState.value.loggedInAs)
    }

    @Test
    fun `logout flips the session to anonymous`() {
        val auth = FakeAuthRepository(initialSession = SessionState.Authenticated("Mario Rossi", "3ªA", "IIS Esempio"))
        val viewModel = SettingsViewModel(FakeSettingsRepository(), auth)

        viewModel.logout()

        assertEquals(1, auth.logoutCalls)
        assertEquals(SessionState.Anonymous, auth.session.value)
        assertTrue(viewModel.uiState.value.isLoggingOut)
        assertNull(viewModel.uiState.value.loggedInAs)
    }
}
