package it.hydr4.argonaut.data

import it.hydr4.argo.ArgoClient
import it.hydr4.argo.api.ArgoClientConfig
import it.hydr4.argo.models.Dashboard
import it.hydr4.argo.models.OrarioSlot
import it.hydr4.argo.models.Profile
import it.hydr4.argo.storage.InMemoryTokenStore
import it.hydr4.argonaut.data.storage.DashboardSnapshotCache
import it.hydr4.argonaut.testing.FakeArgoEngine
import it.hydr4.argonaut.testing.FakeSettingsRepository
import it.hydr4.argonaut.testing.Fixtures
import it.hydr4.argonaut.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.ZoneOffset

/**
 * End-to-end wiring test: the real Argos client (over a fake transport that
 * replays recorded fixtures) drives the real Argonaut repositories. This is
 * the "fake Argos client" integration suite — no Android dependencies, pure JVM.
 */
class ArgonautWiringIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun fullEngine(): FakeArgoEngine = FakeArgoEngine(
        "oauth2/auth" to { FakeArgoEngine.redirect("https://auth.portaleargo.it/login?login_challenge=ch-123") },
        "/auth/sso/login" to { FakeArgoEngine.redirect("it.argosoft.didup.famiglia.new://login-callback?code=code-abc") },
        "oauth2/token" to { FakeArgoEngine.json(Fixtures.text("oauth-token-success.json")) },
        "appfamiglia/api/rest/login" to { FakeArgoEngine.json(Fixtures.text("login-family-success.json")) },
        "api/rest/profilo" to { FakeArgoEngine.json(Fixtures.text("profile-success.json")) },
        "dashboard/dashboard" to { FakeArgoEngine.json(Fixtures.text("dashboard-full.json")) },
        "dashboard/aggiornadata" to { FakeArgoEngine.json("""{"success":true}""") },
        "dashboard/what" to { FakeArgoEngine.json(Fixtures.text("dashboard-what-clean.json")) },
        "orario-giorno" to { FakeArgoEngine.json(Fixtures.text("orario-giorno.json")) },
    )

    private fun client(engine: FakeArgoEngine): ArgoClient = ArgoClient.create(
        config = ArgoClientConfig(),
        engine = engine,
        storage = InMemoryTokenStore(),
        clock = Clock.fixed(FakeArgoEngine.SERVER_INSTANT, ZoneOffset.UTC),
    )

    @Test
    fun `login runs the full flow and lands in an authenticated session`() = runTest {
        val engine = fullEngine()
        val authRepository = DefaultAuthRepository(
            client = client(engine),
            settings = FakeSettingsRepository(),
            snapshotStore = InMemorySnapshotCache(),
        )

        val failure = authRepository.login("ABCDEF", "mario.rossi", "s3cret")

        assertEquals(null, failure)
        assertTrue(authRepository.session.value is SessionState.Authenticated)
        assertEquals("ROSSI LUCA", (authRepository.session.value as SessionState.Authenticated).studentName)
        // The credential flow really hit every step of the SSO dance.
        engine.requestsTo("oauth2/auth")
        engine.requestsTo("oauth2/token")
        engine.requestsTo("api/rest/profilo")
        engine.requestsTo("dashboard/dashboard")
    }

    @Test
    fun `restore session rehydrates from the token store`() = runTest {
        val engine = fullEngine()
        val store = InMemoryTokenStore()
        val client = ArgoClient.create(
            config = ArgoClientConfig(),
            engine = engine,
            storage = store,
            clock = Clock.fixed(FakeArgoEngine.SERVER_INSTANT, ZoneOffset.UTC),
        )
        val authRepository = DefaultAuthRepository(client, FakeSettingsRepository(), InMemorySnapshotCache())
        authRepository.login("ABCDEF", "mario.rossi", "s3cret")

        // A brand-new repository over the same store restores without network.
        val fresh = DefaultAuthRepository(client, FakeSettingsRepository(), InMemorySnapshotCache())
        fresh.restoreSession()

        assertTrue(fresh.session.value is SessionState.Authenticated)
    }

    @Test
    fun `dashboard refresh maps the wire snapshot into the UI summary`() = runTest {
        val engine = fullEngine()
        val client = client(engine)
        val auth = DefaultAuthRepository(client, FakeSettingsRepository(), InMemorySnapshotCache())
        auth.login("ABCDEF", "mario.rossi", "s3cret")

        val repository = DefaultDashboardRepository(client, InMemorySnapshotCache())
        val result = repository.refresh(forceRefresh = true)

        assertTrue(result is RefreshResult.Success)
        val summary = (result as RefreshResult.Success).summary
        assertEquals(7.25, summary.overallAverage!!, 0.001)
        assertTrue(summary.recentGrades.isNotEmpty())
        assertEquals("MATEMATICA", summary.recentGrades.first().subject)
    }

    @Test
    fun `schedule maps the daily timetable slots`() = runTest {
        val engine = fullEngine()
        val client = client(engine)
        val auth = DefaultAuthRepository(client, FakeSettingsRepository(), InMemorySnapshotCache())
        auth.login("ABCDEF", "mario.rossi", "s3cret")

        val repository = DefaultDashboardRepository(client, InMemorySnapshotCache())
        val slots = repository.todaySchedule(forceRefresh = true)

        assertTrue(slots.isNotEmpty())
        assertTrue(slots.first().hour > 0)
    }

    @Test
    fun `logout clears the session and the snapshot cache`() = runTest {
        val engine = fullEngine()
        val client = client(engine)
        val cache = InMemorySnapshotCache()
        val auth = DefaultAuthRepository(client, FakeSettingsRepository(), cache)
        auth.login("ABCDEF", "mario.rossi", "s3cret")
        assertNotNull(cache.loadProfile())

        auth.logout()

        assertEquals(SessionState.Anonymous, auth.session.value)
        assertEquals(null, cache.loadProfile())
        assertEquals(null, cache.loadDashboard())
    }
}

/** In-memory [DashboardSnapshotCache] for JVM tests. */
class InMemorySnapshotCache : DashboardSnapshotCache {
    private var dashboard: Dashboard? = null
    private var schedule: List<OrarioSlot> = emptyList()
    private var profile: Profile? = null

    override fun loadDashboard(): Dashboard? = dashboard

    override fun loadSchedule(): List<OrarioSlot> = schedule

    override fun loadProfile(): Profile? = profile

    override fun save(dashboard: Dashboard?, schedule: List<OrarioSlot>) {
        this.dashboard = dashboard
        this.schedule = schedule
    }

    override fun saveProfile(profile: Profile?) {
        this.profile = profile
    }

    override fun clear() {
        dashboard = null
        schedule = emptyList()
        profile = null
    }
}
