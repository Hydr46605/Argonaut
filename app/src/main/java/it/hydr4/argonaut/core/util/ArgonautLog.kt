package it.hydr4.argonaut.core.util

/**
 * Central log tag. Diagnostics are always sanitized: Argos guarantees tokens
 * never appear in exception messages, and we never log payloads or headers.
 */
object ArgonautLog {
    const val TAG = "Argonaut"
}
