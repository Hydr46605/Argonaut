@file:Suppress("FunctionNaming", "UnusedPrivateMember") // PascalCase composables; @Preview members are tooling-invoked.

package it.hydr4.argonaut.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.hydr4.argonaut.data.model.VotoItem
import it.hydr4.argonaut.ui.theme.ArgonautTheme

/**
 * One grade row: leading [GradeBadge], subject, teacher and date as
 * supporting text, optional comment as the third line.
 */
@Composable
fun VotoListItem(
    voto: VotoItem,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GradeBadge(value = voto.value)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = voto.subject,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val supporting = buildList {
                    voto.teacher?.takeIf { it.isNotBlank() }?.let(::add)
                    voto.date?.let { add(it.toString()) }
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
                voto.comment?.takeIf { it.isNotBlank() }?.let { comment ->
                    Text(
                        text = comment,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) { content() }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) { content() }
    }
}

@Preview(name = "Voto item - light", showBackground = true)
@Composable
private fun VotoListItemLightPreview() {
    ArgonautTheme(darkTheme = false) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            VotoListItem(
                VotoItem(subject = "Matematica", value = 7.5, teacher = "Prof. Bianchi", date = java.time.LocalDate.of(2026, 8, 27)),
            )
            VotoListItem(
                VotoItem(subject = "Storia dell'arte", value = 4.0, teacher = "Prof.ssa Verdi", comment = "Verifica orale da recuperare"),
            )
        }
    }
}

@Preview(name = "Voto item - dark", showBackground = true, backgroundColor = 0xFF101318)
@Composable
private fun VotoListItemDarkPreview() {
    ArgonautTheme(darkTheme = true) {
        VotoListItem(VotoItem(subject = "Fisica", value = 9.0, teacher = "Prof. Neri", date = java.time.LocalDate.of(2026, 8, 26)))
    }
}
