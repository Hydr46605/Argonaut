package it.hydr4.argonaut.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Date and time rendering for the dashboard and list items. */
object TimeText {
    private val locale = Locale.ITALY
    private val mediumDate: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    private val shortTime: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", locale)

    fun date(date: LocalDate): String = date.format(mediumDate)

    fun shortDate(date: LocalDate): String = DateTimeFormatter.ofPattern("dd/MM", locale).format(date)

    fun time(instant: Instant): String = instant.atZone(ZoneId.systemDefault()).format(shortTime)

    /** Shortens "2026-08-27" style wire strings when parseable, else passes through. */
    fun wireDateOrRaw(raw: String?): String? {
        if (raw == null) return null
        return runCatching { LocalDate.parse(raw.take(10)) }.getOrNull()?.let(::shortDate) ?: raw
    }
}
