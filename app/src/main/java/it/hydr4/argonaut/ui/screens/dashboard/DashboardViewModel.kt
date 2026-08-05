package it.hydr4.argonaut.ui.screens.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.hydr4.argonaut.core.util.ArgonautLog
import it.hydr4.argonaut.data.AuthRepository
import it.hydr4.argonaut.data.DashboardRepository
import it.hydr4.argonaut.data.RefreshFailure
import it.hydr4.argonaut.data.RefreshResult
import it.hydr4.argonaut.data.model.DashboardSummary
import it.hydr4.argonaut.data.model.ScheduleSlotItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Whole dashboard screen state. */
sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class Success(
        val summary: DashboardSummary,
        val schedule: List<ScheduleSlotItem>,
        val isRefreshing: Boolean = false,
    ) : DashboardUiState

    data class Error(
        val failure: RefreshFailure,
        val cached: DashboardSummary? = null,
    ) : DashboardUiState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val mutableState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = mutableState.asStateFlow()

    init {
        // Cache-first: render instantly from the last sync, then refresh.
        val cached = dashboardRepository.cachedSummary()
        if (cached != null) {
            mutableState.value = DashboardUiState.Success(
                summary = cached,
                schedule = dashboardRepository.cachedScheduleItems(),
            )
        }
        refresh()
    }

    /** Pull-to-refresh and retry entry point. */
    fun refresh() {
        viewModelScope.launch {
            val previous = mutableState.value
            mutableState.value = when (previous) {
                is DashboardUiState.Success -> previous.copy(isRefreshing = true)
                else -> DashboardUiState.Loading
            }

            val result = dashboardRepository.refresh(forceRefresh = true)
            Log.i(ArgonautLog.TAG, "dashboard refresh: $result")
            when (result) {
                is RefreshResult.Success -> {
                    val schedule = dashboardRepository.todaySchedule(forceRefresh = true)
                    mutableState.value = DashboardUiState.Success(
                        summary = result.summary,
                        schedule = schedule,
                    )
                }
                is RefreshResult.Failure -> {
                    if (result.reason == RefreshFailure.SESSION_EXPIRED) {
                        // The nav graph reacts to the session flow and sends the
                        // user back to login.
                        authRepository.invalidateSession()
                    } else {
                        mutableState.value = DashboardUiState.Error(
                            failure = result.reason,
                            cached = dashboardRepository.cachedSummary(),
                        )
                    }
                }
            }
        }
    }
}
