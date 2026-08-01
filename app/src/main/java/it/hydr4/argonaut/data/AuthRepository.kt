package it.hydr4.argonaut.data

import kotlinx.coroutines.flow.StateFlow

/** Coarse-grained session lifecycle consumed by the UI layer. */
sealed interface SessionState {
    /** Restoring the persisted session at startup. */
    data object Restoring : SessionState

    /** No usable session: the login screen is the start destination. */
    data object Anonymous : SessionState

    /** A restored or freshly created session with an authenticated profile. */
    data class Authenticated(
        val studentName: String,
        val className: String,
        val schoolName: String,
    ) : SessionState
}

/** Failure taxonomy for the login screen; sanitized, never exception-bearing. */
sealed interface LoginFailure {
    data object InvalidCredentials : LoginFailure
    data object Network : LoginFailure
    data object Server : LoginFailure
    data object Unknown : LoginFailure
}

/**
 * Application-specific authentication boundary. Argonaut never talks to
 * Argos's auth machinery directly from ViewModels — everything goes through
 * this interface, which the DI graph binds to the Argos-backed implementation.
 */
interface AuthRepository {

    val session: StateFlow<SessionState>

    /** Rehydrates the encrypted session without network I/O. */
    suspend fun restoreSession()

    /**
     * Runs the full credential flow; returns a [LoginFailure] on failure, or
     * `null` when the session became [SessionState.Authenticated].
     */
    suspend fun login(schoolCode: String, username: String, password: String): LoginFailure?

    /** Marks the session as ended without upstream calls (session-expiry path). */
    fun invalidateSession()

    /** Logs out upstream (best-effort) and wipes local session material. */
    suspend fun logout()
}
