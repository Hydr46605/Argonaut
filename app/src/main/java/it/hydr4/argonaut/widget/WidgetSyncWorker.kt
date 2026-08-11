package it.hydr4.argonaut.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import it.hydr4.argonaut.data.DashboardRepository
import it.hydr4.argonaut.data.RefreshFailure
import it.hydr4.argonaut.data.RefreshResult
import it.hydr4.argonaut.data.storage.WidgetSnapshotStore

/**
 * Background sync for the widgets. Delegates to [DashboardRepository] (which
 * wraps Argos), mirrors the freshest snapshot into [WidgetSnapshotStore] and
 * tells every widget to re-render. Runs on WorkManager's periodic schedule, so
 * it respects Android's background throttling by construction.
 */
@HiltWorker
class WidgetSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dashboardRepository: DashboardRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Logged-out users have nothing to sync; skip quietly.
        if (!isAuthenticated()) {
            return Result.success()
        }

        val dashboardResult = dashboardRepository.refresh(forceRefresh = false)
        dashboardRepository.todaySchedule(forceRefresh = true)

        // A session that died upstream stops the polling loop quietly: the
        // widgets show a sign-in prompt until the user logs in again.
        val sessionAlive = dashboardResult !is RefreshResult.Failure ||
            dashboardResult.reason != RefreshFailure.SESSION_EXPIRED
        if (sessionAlive) {
            updateWidgets()
        }
        return Result.success()
    }

    private suspend fun updateWidgets() {
        val widgets: List<GlanceAppWidget> = listOf(GradeWidget(), ScheduleWidget(), BulletinWidget())
        widgets.forEach { widget -> widget.updateAll(applicationContext) }
    }

    private fun isAuthenticated(): Boolean = WidgetSnapshotStore(applicationContext).loadProfile() != null
}
