package it.hydr4.argonaut.core.util

import java.util.Locale

/** Formats grade values the Italian way: `8.4`, `10` → `8,4`, `10`. */
object GradeFormatter {
    private val locale = Locale.ITALY

    fun format(value: Double): String {
        val rounded = (value * 10).let { Math.round(it) / 10.0 }
        return if (rounded % 1.0 == 0.0) {
            rounded.toInt().toString()
        } else {
            String.format(locale, "%.1f", rounded)
        }
    }

    /** `null` average renders as an em dash. */
    fun formatOrDash(value: Double?): String = value?.let(::format) ?: "—"
}
