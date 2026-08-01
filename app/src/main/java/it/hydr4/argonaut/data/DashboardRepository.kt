package it.hydr4.argonaut.data

import it.hydr4.argonaut.data.model.DashboardSummary
import it.hydr4.argonaut.data.model.ScheduleSlotItem

/** Outcome of a dashboard refresh round. */
sealed interface RefreshResult {
    data class Success(val summary: DashboardSummary) : RefreshResult
    data class Failure(val reason: RefreshFailure) : RefreshResult
}

/** Sanitized failure taxonomy for the dashboard screen. */
enum class RefreshFailure {
    /** Upstream rejected the session; the user must log in again. */
    SESSION_EXPIRED,

    /** Transport-level failure; retry is meaningful. */
    NETWORK,

    /** The register answered but with an unexpected shape; retryable. */
    SERVER,
}

/**
 * Application-specific dashboard boundary. Every call delegates to Argos via
 * the DI-bound implementation; the ViewModel never touches the library.
 */
interface DashboardRepository {

    /** Last known snapshot, served instantly from cache without network I/O. */
    fun cachedSummary(): DashboardSummary?

    /** Last known schedule, served from cache without network I/O. */
    fun cachedScheduleItems(): List<ScheduleSlotItem>

    /** Synchronizes with Argo (change-probe + fetch) and mirrors the snapshot to the widgets. */
    suspend fun refresh(forceRefresh: Boolean = false): RefreshResult

    /** Today's timetable; falls back to the cached slots on failure. */
    suspend fun todaySchedule(forceRefresh: Boolean = false): List<ScheduleSlotItem>
}
