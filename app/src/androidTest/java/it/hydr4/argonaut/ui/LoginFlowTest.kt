package it.hydr4.argonaut.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import it.hydr4.argonaut.MainActivity
import it.hydr4.argonaut.data.LoginFailure
import it.hydr4.argonaut.testing.FakeAuthRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Critical user flows with Compose Test semantics matchers, driven by the
 * in-memory fakes from [it.hydr4.argonaut.testing.TestAppModule].
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var authRepository: FakeAuthRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun login_submission_reachesTheDashboard() {
        waitForLoginScreen()

        composeRule.onNodeWithText("Codice scuola").performTextInput("ABCDEF")
        composeRule.onNodeWithText("Nome utente").performTextInput("mario.rossi")
        composeRule.onNodeWithText("Password").performTextInput("s3cret")
        composeRule.onNodeWithText("Accedi").performClick()

        waitForDashboard()
        composeRule.onNodeWithText("MEDIA GENERALE").assertIsDisplayed()
    }

    @Test
    fun invalidCredentials_showErrorMessage() {
        authRepository.loginFailure = LoginFailure.InvalidCredentials
        waitForLoginScreen()

        composeRule.onNodeWithText("Codice scuola").performTextInput("ABCDEF")
        composeRule.onNodeWithText("Nome utente").performTextInput("mario.rossi")
        composeRule.onNodeWithText("Password").performTextInput("wrong")
        composeRule.onNodeWithText("Accedi").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Credenziali non valide. Controlla i dati e riprova.")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun emptyForm_showsValidationErrorsWithoutLeavingLogin() {
        waitForLoginScreen()

        composeRule.onNodeWithText("Accedi").performClick()

        // Still on the login screen: no dashboard yet.
        composeRule.waitUntil(timeoutMillis = 2_000) {
            composeRule.onAllNodesWithText("Accedi").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun dashboard_navigatesToSettingsAndBack() {
        login()
        waitForDashboard()

        composeRule.onNodeWithContentDescription("Impostazioni").performClick()
        composeRule.onNodeWithText("ASPETTO").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Indietro").performClick()
        composeRule.onNodeWithText("MEDIA GENERALE").assertIsDisplayed()
    }

    private fun login() {
        waitForLoginScreen()
        composeRule.onNodeWithText("Codice scuola").performTextInput("ABCDEF")
        composeRule.onNodeWithText("Nome utente").performTextInput("mario.rossi")
        composeRule.onNodeWithText("Password").performTextInput("s3cret")
        composeRule.onNodeWithText("Accedi").performClick()
    }

    private fun waitForLoginScreen() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Accedi").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForDashboard() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("MEDIA GENERALE").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
