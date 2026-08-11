package it.hydr4.argonaut.widget

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the periodic [WidgetSyncWorker] schedule. Called on login (and whenever
 * the user changes the refresh cadence); cancelled on logout.
 */
interface WidgetSyncScheduler {
    /** (Re)schedules the worker at the given interval in minutes. */
    fun schedule(refreshMinutes: Int)

    /** Cancels the periodic worker (logout / no session). */
    fun cancel()
}

/**
 * WorkManager implementation. [ExistingPeriodicWorkPolicy.UPDATE] keeps the
 * worker but adopts the new interval, so changing the cadence never spawns
 * duplicate jobs.
 */
@Singleton
class WorkManagerWidgetSyncScheduler @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext context: Context,
) : WidgetSyncScheduler {

    private val workManager = WorkManager.getInstance(context.applicationContext)

    override fun schedule(refreshMinutes: Int) {
        val interval = refreshMinutes.coerceAtLeast(MIN_INTERVAL_MINUTES).toLong()
        val request = PeriodicWorkRequestBuilder<WidgetSyncWorker>(interval, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    override fun cancel() {
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    private companion object {
        const val UNIQUE_WORK_NAME = "argonaut-widget-sync"
        const val MIN_INTERVAL_MINUTES = 15
    }
}
