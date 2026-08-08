@file:Suppress("FunctionNaming", "UnusedPrivateMember") // PascalCase composables; @Preview members are tooling-invoked.

package it.hydr4.argonaut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.hydr4.argonaut.data.model.ScheduleSlotItem
import it.hydr4.argonaut.ui.theme.ArgonautTheme

/**
 * One timetable slot: hour circle, subject and teacher/room.
 */
@Composable
fun ScheduleSlotRow(
    slot: ScheduleSlotItem,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${slot.hour}ª",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = slot.subject.ifBlank { "—" },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val supporting = buildList {
                slot.teacher?.takeIf { it.isNotBlank() }?.let(::add)
                slot.room?.takeIf { it.isNotBlank() }?.let(::add)
            }
            if (supporting.isNotEmpty()) {
                Text(
                    text = supporting.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(name = "Schedule slot - light", showBackground = true)
@Composable
private fun ScheduleSlotLightPreview() {
    ArgonautTheme(darkTheme = false) {
        Column {
            ScheduleSlotRow(ScheduleSlotItem(hour = 1, subject = "Matematica", teacher = "Prof. Bianchi", room = "Aula 12"))
            ScheduleSlotRow(ScheduleSlotItem(hour = 2, subject = "Italiano", teacher = "Prof.ssa Verdi"))
        }
    }
}

@Preview(name = "Schedule slot - dark", showBackground = true, backgroundColor = 0xFF101318)
@Composable
private fun ScheduleSlotDarkPreview() {
    ArgonautTheme(darkTheme = true) {
        ScheduleSlotRow(ScheduleSlotItem(hour = 3, subject = "Fisica", teacher = "Prof. Neri"))
    }
}
