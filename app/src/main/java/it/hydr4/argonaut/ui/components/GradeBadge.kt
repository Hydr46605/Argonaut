@file:Suppress("FunctionNaming", "UnusedPrivateMember") // PascalCase composables; @Preview members are tooling-invoked.

package it.hydr4.argonaut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.hydr4.argonaut.core.util.GradeFormatter
import it.hydr4.argonaut.ui.theme.ArgonautTheme
import it.hydr4.argonaut.ui.theme.GradeColors

/**
 * Squircle chip showing a grade, tinted by the passing/failing threshold so
 * the register reads at a glance.
 */
@Composable
fun GradeBadge(
    value: Double,
    modifier: Modifier = Modifier,
    gradeColors: GradeColors = GradeColors.current(),
    containerColor: Color = if (value >= PASSING_THRESHOLD) gradeColors.passContainer else gradeColors.failContainer,
    contentColor: Color = if (value >= PASSING_THRESHOLD) gradeColors.onPassContainer else gradeColors.onFailContainer,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(containerColor, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = GradeFormatter.format(value),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = contentColor,
        )
    }
}

internal const val PASSING_THRESHOLD = 6.0

@Preview(name = "Grade badge - light", showBackground = true)
@Composable
private fun GradeBadgePassPreview() {
    ArgonautTheme(darkTheme = false) {
        GradeBadge(value = 8.5)
    }
}

@Preview(name = "Grade badge - dark", showBackground = true, backgroundColor = 0xFF101318)
@Composable
private fun GradeBadgeFailPreview() {
    ArgonautTheme(darkTheme = true) {
        GradeBadge(value = 4.5)
    }
}
