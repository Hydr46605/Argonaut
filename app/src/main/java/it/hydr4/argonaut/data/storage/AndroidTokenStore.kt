package it.hydr4.argonaut.data.storage

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import it.hydr4.argo.storage.SessionSnapshot
import it.hydr4.argo.storage.TokenStore
import kotlinx.serialization.json.Json

/**
 * [TokenStore] implementation for Argonaut: the [SessionSnapshot] (bearer,
 * refresh token, login data and profile) is serialized, encrypted with a
 * Keystore-resident AES-256 key and persisted in private SharedPreferences.
 *
 * This is what makes the login fully persistent: Argos restores the session
 * from here on every app start without network I/O.
 */
class AndroidTokenStore(context: Context) : TokenStore {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val cipher = KeystoreAesGcmCipher()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun load(): SessionSnapshot? {
        val blob = prefs.getString(KEY_SNAPSHOT, null) ?: return null
        return runCatching {
            val plain = cipher.decrypt(Base64.decode(blob, Base64.NO_WRAP))
            json.decodeFromString(SessionSnapshot.serializer(), plain.decodeToString())
        }.getOrNull()
    }

    override suspend fun save(snapshot: SessionSnapshot) {
        val plain = json.encodeToString(SessionSnapshot.serializer(), snapshot)
        val blob = Base64.encodeToString(cipher.encrypt(plain.encodeToByteArray()), Base64.NO_WRAP)
        prefs.edit { putString(KEY_SNAPSHOT, blob) }
    }

    override suspend fun clear() {
        prefs.edit { remove(KEY_SNAPSHOT) }
    }

    private companion object {
        const val PREFS_NAME = "argonaut_session"
        const val KEY_SNAPSHOT = "encrypted_session_snapshot"
    }
}
