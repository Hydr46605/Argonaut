@file:Suppress("FunctionNaming", "UnusedPrivateMember") // PascalCase composables; @Preview members are tooling-invoked.

package it.hydr4.argonaut.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.hydr4.argonaut.ui.theme.ArgonautTheme

/**
 * Section title rendered on the surface color so it survives as a sticky
 * header over scrolling content.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            trailing?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(name = "Section header", showBackground = true)
@Composable
private fun SectionHeaderPreview() {
    ArgonautTheme(darkTheme = false) {
        SectionHeader(title = "Ultimi voti", trailing = "5")
    }
}
