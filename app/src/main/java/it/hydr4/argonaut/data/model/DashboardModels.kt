package it.hydr4.argonaut.data.model

import java.time.Instant
import java.time.LocalDate

/**
 * Presentation-ready dashboard snapshot. Everything the dashboard and the
 * widgets render is derived from this immutable model, never from Argos DTOs.
 */
data class DashboardSummary(
    val fetchedAt: Instant? = null,
    val overallAverage: Double? = null,
    val recentGrades: List<VotoItem> = emptyList(),
    val absences: List<AbsenceItem> = emptyList(),
    val reminders: List<ReminderItem> = emptyList(),
    val bulletins: List<BulletinItem> = emptyList(),
) {
    val hasContent: Boolean
        get() = recentGrades.isNotEmpty() || absences.isNotEmpty() || reminders.isNotEmpty()
}

/** One grade row: subject, value, event date and teacher. */
data class VotoItem(
    val subject: String,
    val value: Double,
    val date: LocalDate? = null,
    val teacher: String? = null,
    val comment: String? = null,
)

/** One daily-timetable slot. */
data class ScheduleSlotItem(
    val hour: Int,
    val subject: String,
    val teacher: String? = null,
    val room: String? = null,
)

/** One attendance row from the register. */
data class AbsenceItem(
    val date: String? = null,
    val justifiable: Boolean = false,
    val justified: Boolean = false,
    val note: String? = null,
)

/** One teacher reminder ("promemoria") visible to the family. */
data class ReminderItem(
    val text: String,
    val teacher: String? = null,
    val date: String? = null,
)

/** One bulletin entry, teacher board ("bacheca") or student board. */
data class BulletinItem(
    val title: String,
    val author: String? = null,
    val date: String? = null,
    val requiresPv: Boolean = false,
)
