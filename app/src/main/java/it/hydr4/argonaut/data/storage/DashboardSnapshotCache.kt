package it.hydr4.argonaut.data.storage

import it.hydr4.argo.models.Dashboard
import it.hydr4.argo.models.OrarioSlot
import it.hydr4.argo.models.Profile

/**
 * Persistence boundary for the freshest register snapshot. Repositories write
 * through here after every sync; widgets and cold starts read from it, so
 * neither performs network I/O to render.
 */
interface DashboardSnapshotCache {
    fun loadDashboard(): Dashboard?
    fun loadSchedule(): List<OrarioSlot>
    fun loadProfile(): Profile?
    fun save(dashboard: Dashboard?, schedule: List<OrarioSlot>)
    fun saveProfile(profile: Profile?)
    fun clear()
}
