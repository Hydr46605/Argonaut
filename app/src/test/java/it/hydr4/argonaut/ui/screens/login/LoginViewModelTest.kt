package it.hydr4.argonaut.ui.screens.login

import it.hydr4.argonaut.data.LoginFailure
import it.hydr4.argonaut.domain.CredentialValidator
import it.hydr4.argonaut.testing.FakeAuthRepository
import it.hydr4.argonaut.testing.FakeSettingsRepository
import it.hydr4.argonaut.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `empty form submit surfaces field errors without calling the repository`() {
        val auth = FakeAuthRepository()
        val viewModel = LoginViewModel(auth, FakeSettingsRepository(), CredentialValidator())

        viewModel.submit()

        val state = viewModel.uiState.value as LoginUiState.Ready
        assertNotNull(state.schoolCodeError)
        assertNotNull(state.usernameError)
        assertNotNull(state.passwordError)
        assertEquals(0, auth.loginCalls)
    }

    @Test
    fun `successful login clears the failure and keeps submitting flag off`() {
        val auth = FakeAuthRepository()
        val viewModel = LoginViewModel(auth, FakeSettingsRepository(), CredentialValidator())

        viewModel.onSchoolCodeChange("ABCDEF")
        viewModel.onUsernameChange("mario.rossi")
        viewModel.onPasswordChange("s3cret")
        viewModel.submit()

        val state = viewModel.uiState.value as LoginUiState.Ready
        assertFalse(state.isSubmitting)
        assertNull(state.failure)
        assertEquals(1, auth.loginCalls)
        assertEquals("ABCDEF", auth.lastLogin?.first)
    }

    @Test
    fun `invalid credentials are reported and the password field is cleared`() {
        val auth = FakeAuthRepository(loginFailure = LoginFailure.InvalidCredentials)
        val viewModel = LoginViewModel(auth, FakeSettingsRepository(), CredentialValidator())

        viewModel.onSchoolCodeChange("ABCDEF")
        viewModel.onUsernameChange("mario.rossi")
        viewModel.onPasswordChange("wrong")
        viewModel.submit()

        val state = viewModel.uiState.value as LoginUiState.Ready
        assertEquals(LoginFailure.InvalidCredentials, state.failure)
        assertEquals("", state.password)
        assertFalse(state.isSubmitting)
    }

    @Test
    fun `network failure is reported distinctly`() {
        val auth = FakeAuthRepository(loginFailure = LoginFailure.Network)
        val viewModel = LoginViewModel(auth, FakeSettingsRepository(), CredentialValidator())

        viewModel.onSchoolCodeChange("ABCDEF")
        viewModel.onUsernameChange("mario.rossi")
        viewModel.onPasswordChange("s3cret")
        viewModel.submit()

        assertEquals(LoginFailure.Network, (viewModel.uiState.value as LoginUiState.Ready).failure)
    }

    @Test
    fun `school code is prefilled from settings`() {
        val settings = FakeSettingsRepository().apply { setLastSchoolCode("XY1234") }
        val viewModel = LoginViewModel(FakeAuthRepository(), settings, CredentialValidator())

        assertEquals("XY1234", (viewModel.uiState.value as LoginUiState.Ready).schoolCode)
    }

    @Test
    fun `typing clears the previous error`() {
        val auth = FakeAuthRepository(loginFailure = LoginFailure.InvalidCredentials)
        val viewModel = LoginViewModel(auth, FakeSettingsRepository(), CredentialValidator())
        viewModel.onSchoolCodeChange("ABCDEF")
        viewModel.onUsernameChange("mario.rossi")
        viewModel.onPasswordChange("wrong")
        viewModel.submit()
        assertTrue((viewModel.uiState.value as LoginUiState.Ready).failure != null)

        viewModel.onPasswordChange("s3cret")

        assertNull((viewModel.uiState.value as LoginUiState.Ready).failure)
    }
}
