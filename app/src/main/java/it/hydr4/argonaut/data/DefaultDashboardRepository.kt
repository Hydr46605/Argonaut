package it.hydr4.argonaut.data

import android.util.Log
import it.hydr4.argo.ArgoClient
import it.hydr4.argo.exceptions.AuthenticationException
import it.hydr4.argo.exceptions.NetworkException
import it.hydr4.argo.models.OrarioSlot
import it.hydr4.argonaut.core.util.ArgonautLog
import it.hydr4.argonaut.data.mapping.DashboardMapper
import it.hydr4.argonaut.data.model.DashboardSummary
import it.hydr4.argonaut.data.model.ScheduleSlotItem
import it.hydr4.argonaut.data.storage.DashboardSnapshotCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Argos-backed [DashboardRepository]. The freshest snapshot is mirrored into
 * [WidgetSnapshotStore] so widgets and cold starts never perform network I/O.
 */
@Singleton
class DefaultDashboardRepository @Inject constructor(
    private val client: ArgoClient,
    private val snapshotStore: DashboardSnapshotCache,
) : DashboardRepository {

    override fun cachedSummary(): DashboardSummary? = snapshotStore.loadDashboard()?.let(DashboardMapper::toSummary)

    override fun cachedScheduleItems(): List<ScheduleSlotItem> = cachedSchedule().map(DashboardMapper::toScheduleSlot)

    override suspend fun refresh(forceRefresh: Boolean): RefreshResult = withContext(Dispatchers.IO) {
        runCatching {
            val (dashboard, _) = client.synchronize(forceRefresh = forceRefresh)
            dashboard
        }.fold(
            onSuccess = { dashboard ->
                if (dashboard != null) {
                    snapshotStore.save(dashboard = dashboard, schedule = cachedSchedule())
                    RefreshResult.Success(DashboardMapper.toSummary(dashboard))
                } else {
                    val cached = cachedSummary()
                    if (cached != null) {
                        RefreshResult.Success(cached)
                    } else {
                        RefreshResult.Failure(RefreshFailure.SERVER)
                    }
                }
            },
            onFailure = { throwable ->
                Log.w(ArgonautLog.TAG, "refresh failed: ${throwable::class.simpleName}: ${throwable.message}")
                when (throwable) {
                    is AuthenticationException -> RefreshResult.Failure(RefreshFailure.SESSION_EXPIRED)
                    is NetworkException -> RefreshResult.Failure(RefreshFailure.NETWORK)
                    else -> RefreshResult.Failure(RefreshFailure.SERVER)
                }
            },
        )
    }

    override suspend fun todaySchedule(forceRefresh: Boolean): List<ScheduleSlotItem> = withContext(Dispatchers.IO) {
        val slots: List<OrarioSlot> = if (forceRefresh) {
            runCatching { client.schedule.orarioGiornaliero() }
                .getOrElse { cachedSchedule() }
        } else {
            val cached = cachedSchedule()
            if (cached.isEmpty()) {
                runCatching { client.schedule.orarioGiornaliero() }.getOrDefault(emptyList())
            } else {
                cached
            }
        }
        snapshotStore.save(dashboard = snapshotStore.loadDashboard(), schedule = slots)
        slots.map(DashboardMapper::toScheduleSlot)
    }

    private fun cachedSchedule(): List<OrarioSlot> = snapshotStore.loadSchedule()
}
