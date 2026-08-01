package it.hydr4.argonaut.data.storage

import android.content.Context
import androidx.core.content.edit
import it.hydr4.argo.models.Dashboard
import it.hydr4.argo.models.OrarioSlot
import it.hydr4.argo.models.Profile
import kotlinx.serialization.json.Json

/**
 * SharedPreferences-backed cache of the freshest [Dashboard] and daily
 * schedule. Written by the sync worker after every round and read by the
 * Glance widgets and the app's cold start, so neither ever performs network
 * I/O to render.
 */
class WidgetSnapshotStore(context: Context) : DashboardSnapshotCache {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    /** Last dashboard snapshot, or `null` when never synced. */
    override fun loadDashboard(): Dashboard? {
        val raw = prefs.getString(KEY_DASHBOARD, null) ?: return null
        return runCatching { json.decodeFromString(Dashboard.serializer(), raw) }.getOrNull()
    }

    /** Last daily schedule slots, or `null` when never synced. */
    override fun loadSchedule(): List<OrarioSlot> {
        val raw = prefs.getString(KEY_SCHEDULE, null) ?: return emptyList()
        return runCatching { json.decodeFromString(ScheduleListSerializer, raw) }.getOrNull() ?: emptyList()
    }

    /** Student identity used by the widgets; null when never authenticated. */
    override fun loadProfile(): Profile? {
        val raw = prefs.getString(KEY_PROFILE, null) ?: return null
        return runCatching { json.decodeFromString(Profile.serializer(), raw) }.getOrNull()
    }

    override fun saveProfile(profile: Profile?) {
        prefs.edit {
            if (profile == null) {
                remove(KEY_PROFILE)
            } else {
                putString(KEY_PROFILE, json.encodeToString(Profile.serializer(), profile))
            }
        }
    }

    override fun save(dashboard: Dashboard?, schedule: List<OrarioSlot>) {
        prefs.edit {
            if (dashboard != null) {
                putString(KEY_DASHBOARD, json.encodeToString(Dashboard.serializer(), dashboard))
                putLong(KEY_LAST_SYNC_AT, System.currentTimeMillis())
            }
            putString(KEY_SCHEDULE, json.encodeToString(ScheduleListSerializer, schedule))
        }
    }

    override fun clear() {
        prefs.edit { clear() }
    }

    fun lastSyncAt(): Long = prefs.getLong(KEY_LAST_SYNC_AT, 0L)

    companion object {
        const val PREFS_NAME = "argonaut_widget_snapshot"
        private const val KEY_DASHBOARD = "dashboard_json"
        private const val KEY_SCHEDULE = "schedule_json"
        private const val KEY_PROFILE = "profile_json"
        private const val KEY_LAST_SYNC_AT = "last_sync_at"

        private val ScheduleListSerializer = kotlinx.serialization.builtins.ListSerializer(OrarioSlot.serializer())
    }
}
