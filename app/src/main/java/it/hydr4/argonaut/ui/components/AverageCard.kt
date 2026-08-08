@file:Suppress("FunctionNaming", "UnusedPrivateMember") // PascalCase composables; @Preview members are tooling-invoked.

package it.hydr4.argonaut.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.hydr4.argonaut.R
import it.hydr4.argonaut.core.util.GradeFormatter
import it.hydr4.argonaut.ui.theme.ArgonautTheme

/**
 * Hero card of the dashboard: the `mediaGenerale` as a large, animated number
 * with the student's class and name. The counter eases toward the new value
 * whenever the average updates, and counts up on first appearance.
 */
@Composable
fun AverageCard(
    average: Double?,
    studentName: String?,
    className: String?,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = scheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Text(
                text = stringResource(R.string.dashboard_media_generale).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                AnimatedAverageText(
                    average = average,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(horizontalAlignment = Alignment.End) {
                    studentName?.let { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleSmall,
                            color = scheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    className?.let { clazz ->
                        Text(
                            text = clazz,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onPrimaryContainer.copy(alpha = 0.7f),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedAverageText(average: Double?, modifier: Modifier = Modifier) {
    if (average == null) {
        Text(
            text = "—",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = modifier,
        )
        return
    }
    val animated by animateFloatAsState(
        targetValue = average.toFloat(),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "media-generale",
    )
    Text(
        text = GradeFormatter.format(animated.toDouble()),
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}

@Preview(name = "Average card - light", showBackground = true)
@Composable
private fun AverageCardLightPreview() {
    ArgonautTheme(darkTheme = false) {
        Column {
            AverageCard(average = 8.4, studentName = "Mario Rossi", className = "3ªA")
        }
    }
}

@Preview(name = "Average card - dark", showBackground = true, backgroundColor = 0xFF101318)
@Composable
private fun AverageCardDarkPreview() {
    ArgonautTheme(darkTheme = true) {
        AverageCard(average = null, studentName = "Mario Rossi", className = "3ªA")
    }
}
