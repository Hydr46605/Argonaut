package it.hydr4.argonaut.data.mapping

import it.hydr4.argo.models.AppelloEntry
import it.hydr4.argo.models.BachecaEntry
import it.hydr4.argo.models.Dashboard
import it.hydr4.argo.models.PromemoriaEntry
import it.hydr4.argo.models.Voto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class DashboardMapperTest {

    @Test
    fun `grades map preserving order by event date descending`() {
        val older = voto(pk = "1", value = 7.0, subject = "MATEMATICA", at = "2026-03-01T08:00:00")
        val newer = voto(pk = "2", value = 9.0, subject = "ITALIANO", at = "2026-03-12T08:00:00")

        val summary = DashboardMapper.toSummary(Dashboard(grades = listOf(older, newer)))

        assertEquals(listOf("ITALIANO", "MATEMATICA"), summary.recentGrades.map { it.subject })
        assertEquals(9.0, summary.recentGrades.first().value, 0.001)
    }

    @Test
    fun `grades without value or subject are dropped`() {
        val incomplete = voto(pk = "3", value = null, subject = "STORIA", at = "2026-03-01T08:00:00")
        val summary = DashboardMapper.toSummary(Dashboard(grades = listOf(incomplete)))
        assertTrue(summary.recentGrades.isEmpty())
    }

    @Test
    fun `overall average passes through`() {
        val summary = DashboardMapper.toSummary(Dashboard(overallAverage = 7.25))
        assertEquals(7.25, summary.overallAverage!!, 0.001)
    }

    @Test
    fun `absences map with justification state`() {
        val entry = AppelloEntry(pk = "a1", eventType = "A", justifiable = true, justified = "N", note = "Nota")
        val item = DashboardMapper.toAbsenceItem(entry)
        assertTrue(item.justifiable)
        assertEquals(false, item.justified)
        assertEquals("Nota", item.note)
    }

    @Test
    fun `reminders map their text and teacher`() {
        val entry = PromemoriaEntry(pk = "r1", annotations = "Recupero martedì", teacher = "PROF BIANCHI")
        val item = DashboardMapper.toReminderItem(entry)
        assertEquals("Recupero martedì", item.text)
        assertEquals("PROF BIANCHI", item.teacher)
    }

    @Test
    fun `bulletins map category and author`() {
        val entry = BachecaEntry(pk = "b1", category = "Circolari", author = "Segreteria", requiresPv = true)
        val item = DashboardMapper.toBulletinItem(entry)
        assertEquals("Circolari", item.title)
        assertEquals("Segreteria", item.author)
        assertTrue(item.requiresPv)
    }

    @Test
    fun `recent grades are capped`() {
        val grades = (1..20).map { index ->
            voto(pk = index.toString(), value = index.toDouble(), subject = "MAT", at = "2026-03-01T08:00:00")
        }
        val summary = DashboardMapper.toSummary(Dashboard(grades = grades))
        assertEquals(8, summary.recentGrades.size)
    }

    private fun voto(pk: String, value: Double?, subject: String, at: String): Voto = Voto(
        pk = pk,
        datEvento = LocalDateTime.parse(at),
        value = value,
        subjectName = subject,
    )
}
