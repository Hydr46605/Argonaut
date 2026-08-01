package it.hydr4.argonaut.data.mapping

import it.hydr4.argo.models.AppelloEntry
import it.hydr4.argo.models.BachecaEntry
import it.hydr4.argo.models.Dashboard
import it.hydr4.argo.models.OrarioSlot
import it.hydr4.argo.models.PromemoriaEntry
import it.hydr4.argo.models.Voto
import it.hydr4.argonaut.data.model.AbsenceItem
import it.hydr4.argonaut.data.model.BulletinItem
import it.hydr4.argonaut.data.model.DashboardSummary
import it.hydr4.argonaut.data.model.ReminderItem
import it.hydr4.argonaut.data.model.ScheduleSlotItem
import it.hydr4.argonaut.data.model.VotoItem

/**
 * Maps Argos wire models into presentation models. Deliberately thin: the
 * dashboard screen never sees an Argos DTO, but no business logic lives here.
 */
object DashboardMapper {
    private const val RECENT_GRADES_LIMIT = 8

    fun toSummary(dashboard: Dashboard): DashboardSummary = DashboardSummary(
        fetchedAt = dashboard.fetchedAt,
        overallAverage = dashboard.overallAverage,
        recentGrades = dashboard.grades
            .filter { it.value != null && it.subjectName != null }
            .sortedWith(compareByDescending<Voto> { it.datEvento }.thenByDescending { it.value })
            .take(RECENT_GRADES_LIMIT)
            .map(::toVotoItem),
        absences = dashboard.attendance.map(::toAbsenceItem),
        reminders = dashboard.reminders.map(::toReminderItem),
        bulletins = dashboard.bulletins.map(::toBulletinItem),
    )

    fun toVotoItem(voto: Voto): VotoItem = VotoItem(
        subject = voto.subjectName.orEmpty(),
        value = voto.value ?: 0.0,
        date = voto.datEvento?.toLocalDate(),
        teacher = voto.teacher,
        comment = voto.comment,
    )

    fun toScheduleSlot(slot: OrarioSlot): ScheduleSlotItem = ScheduleSlotItem(
        hour = slot.hourNumber ?: 0,
        subject = slot.subject.orEmpty().ifBlank { slot.classGroup.orEmpty() },
        teacher = slot.teacher ?: listOfNotNull(slot.teacherSurname, slot.teacherName).joinToString(" ").ifBlank { null },
        room = slot.roomTimeRaw,
    )

    fun toAbsenceItem(entry: AppelloEntry): AbsenceItem = AbsenceItem(
        date = entry.occurredOn ?: entry.eventAt,
        justifiable = entry.justifiable,
        justified = entry.justified == "S",
        note = entry.note,
    )

    fun toReminderItem(entry: PromemoriaEntry): ReminderItem = ReminderItem(
        text = entry.annotations.orEmpty(),
        teacher = entry.teacher,
        date = entry.day,
    )

    fun toBulletinItem(entry: BachecaEntry): BulletinItem = BulletinItem(
        title = entry.category.orEmpty().ifBlank { entry.author.orEmpty() }.ifBlank { "Bacheca" },
        author = entry.author,
        date = entry.publishedOn,
        requiresPv = entry.requiresPv,
    )
}
