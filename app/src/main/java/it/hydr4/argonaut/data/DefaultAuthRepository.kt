package it.hydr4.argonaut.data

import android.util.Log
import it.hydr4.argo.ArgoClient
import it.hydr4.argo.exceptions.ArgoException
import it.hydr4.argo.exceptions.AuthenticationException
import it.hydr4.argo.exceptions.NetworkException
import it.hydr4.argo.models.Credentials
import it.hydr4.argo.models.Profile
import it.hydr4.argonaut.core.util.ArgonautLog
import it.hydr4.argonaut.data.storage.DashboardSnapshotCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Argos-backed [AuthRepository]. The encrypted session lives in
 * [it.hydr4.argonaut.data.storage.AndroidTokenStore], which Argos writes
 * through on every token/profile change, so [restoreSession] brings the user
 * straight back into the dashboard across app restarts.
 */
@Singleton
class DefaultAuthRepository @Inject constructor(
    private val client: ArgoClient,
    private val settings: SettingsRepository,
    private val snapshotStore: DashboardSnapshotCache,
) : AuthRepository {

    private val mutableSession = MutableStateFlow<SessionState>(SessionState.Restoring)

    override val session: StateFlow<SessionState> = mutableSession.asStateFlow()

    override suspend fun restoreSession() {
        val restored = runCatching { client.restorePersistedSession() }.getOrDefault(false)
        val profile = client.profiles.currentOrNull()
        val next = if (restored && profile != null) {
            snapshotStore.saveProfile(profile)
            authenticatedState(profile)
        } else {
            SessionState.Anonymous
        }
        Log.i(ArgonautLog.TAG, "restoreSession: restored=$restored -> ${next.javaClass.simpleName}")
        mutableSession.value = next
    }

    override suspend fun login(schoolCode: String, username: String, password: String): LoginFailure? = withContext(Dispatchers.IO) {
        runCatching {
            client.login(Credentials(schoolCode = schoolCode, username = username, password = password))
        }.fold(
            onSuccess = {
                settings.setLastSchoolCode(schoolCode)
                val profile = client.profiles.currentOrNull()
                if (profile != null) {
                    snapshotStore.saveProfile(profile)
                    mutableSession.value = authenticatedState(profile)
                } else {
                    mutableSession.value = SessionState.Anonymous
                }
                null
            },
            onFailure = { throwable ->
                Log.w(ArgonautLog.TAG, "login failed: ${throwable::class.simpleName}: ${throwable.message}")
                classify(throwable)
            },
        )
    }

    override fun invalidateSession() {
        mutableSession.value = SessionState.Anonymous
    }

    override suspend fun logout() {
        runCatching { client.logout() }
        snapshotStore.clear()
        mutableSession.value = SessionState.Anonymous
    }

    private fun classify(throwable: Throwable): LoginFailure = when (throwable) {
        is AuthenticationException -> LoginFailure.InvalidCredentials
        is NetworkException -> LoginFailure.Network
        is ArgoException -> LoginFailure.Server
        else -> LoginFailure.Unknown
    }
}

private fun authenticatedState(profile: Profile): SessionState.Authenticated = SessionState.Authenticated(
    studentName = profile.alunno.nominativo.ifBlank { profile.alunno.nome },
    className = profile.scheda.classe.denomination,
    schoolName = profile.scheda.scuola.description,
)
