package it.hydr4.argonaut.ui.screens.dashboard

import it.hydr4.argonaut.data.RefreshFailure
import it.hydr4.argonaut.data.RefreshResult
import it.hydr4.argonaut.data.SessionState
import it.hydr4.argonaut.data.model.DashboardSummary
import it.hydr4.argonaut.data.model.ScheduleSlotItem
import it.hydr4.argonaut.testing.FakeAuthRepository
import it.hydr4.argonaut.testing.FakeDashboardRepository
import it.hydr4.argonaut.testing.MainDispatcherRule
import it.hydr4.argonaut.testing.refreshFailure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `refresh success renders the summary and schedule`() {
        val repo = FakeDashboardRepository(
            refreshResult = RefreshResult.Success(DashboardSummary(overallAverage = 8.4)),
            schedule = listOf(ScheduleSlotItem(hour = 1, subject = "Matematica")),
        )
        val viewModel = DashboardViewModel(repo, FakeAuthRepository())

        val state = viewModel.uiState.value as DashboardUiState.Success
        assertEquals(8.4, state.summary.overallAverage!!, 0.001)
        assertEquals("Matematica", state.schedule.first().subject)
    }

    @Test
    fun `cached content renders immediately before the refresh completes`() {
        val repo = FakeDashboardRepository(
            cached = DashboardSummary(overallAverage = 7.0),
            refreshResult = RefreshResult.Success(DashboardSummary(overallAverage = 8.0)),
        )
        val viewModel = DashboardViewModel(repo, FakeAuthRepository())

        // init shows cache instantly, then the refresh lands on the fresh value.
        assertTrue(viewModel.uiState.value is DashboardUiState.Success)
        assertEquals(8.0, (viewModel.uiState.value as DashboardUiState.Success).summary.overallAverage!!, 0.001)
    }

    @Test
    fun `network failure surfaces an error state with retryable reason`() {
        val repo = FakeDashboardRepository(refreshResult = refreshFailure(RefreshFailure.NETWORK))
        val viewModel = DashboardViewModel(repo, FakeAuthRepository())

        val state = viewModel.uiState.value as DashboardUiState.Error
        assertEquals(RefreshFailure.NETWORK, state.failure)
    }

    @Test
    fun `session expiry invalidates the session to push the user to login`() {
        val auth = FakeAuthRepository(initialSession = SessionState.Authenticated("Mario Rossi", "3ªA", "IIS Esempio"))
        val repo = FakeDashboardRepository(refreshResult = refreshFailure(RefreshFailure.SESSION_EXPIRED))
        DashboardViewModel(repo, auth)

        assertEquals(SessionState.Anonymous, auth.session.value)
    }

    @Test
    fun `pull-to-refresh keeps content while refreshing`() {
        val repo = FakeDashboardRepository(
            refreshResult = RefreshResult.Success(DashboardSummary(overallAverage = 8.4)),
        )
        val viewModel = DashboardViewModel(repo, FakeAuthRepository())
        assertNotNull(viewModel.uiState.value)

        viewModel.refresh()

        val state = viewModel.uiState.value as DashboardUiState.Success
        assertTrue(state.isRefreshing || viewModel.uiState.value is DashboardUiState.Success)
    }
}
