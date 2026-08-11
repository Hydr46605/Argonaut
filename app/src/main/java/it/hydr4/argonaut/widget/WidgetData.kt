package it.hydr4.argonaut.widget

import android.content.Context
import it.hydr4.argo.models.BachecaEntry
import it.hydr4.argo.models.Dashboard
import it.hydr4.argo.models.OrarioSlot
import it.hydr4.argo.models.Profile
import it.hydr4.argonaut.data.storage.WidgetSnapshotStore

/**
 * Blocking reads of the cached snapshot for Glance compositions (widgets render
 * on a background thread, so SharedPreferences access is safe here).
 */
object WidgetData {

    fun dashboard(context: Context): Dashboard? = WidgetSnapshotStore(context).loadDashboard()

    fun schedule(context: Context): List<OrarioSlot> = WidgetSnapshotStore(context).loadSchedule()

    fun bulletins(context: Context): List<BachecaEntry> = dashboard(context)?.bulletins.orEmpty().take(BULLETIN_LIMIT)

    /** Student identity for the header line. */
    fun profile(context: Context): Profile? = WidgetSnapshotStore(context).loadProfile()

    /** Whether a session exists; widgets show a sign-in prompt otherwise. */
    fun hasSession(context: Context): Boolean = dashboard(context) != null || schedule(context).isNotEmpty()

    private const val BULLETIN_LIMIT = 12
}
