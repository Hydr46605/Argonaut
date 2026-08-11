@file:Suppress("FunctionNaming") // Glance composables are PascalCase by convention.

package it.hydr4.argonaut.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import it.hydr4.argonaut.core.util.GradeFormatter

/**
 * Small, reusable layout pieces that give every widget the same visual
 * grammar: a primary-colored section header, hairline dividers, grade values
 * colored by threshold, and rounded hour badges.
 */

/** Section header: uppercase primary label. */
@Composable
internal fun WidgetHeader(
    text: String,
    theme: WidgetColorScheme,
    modifier: GlanceModifier = GlanceModifier,
) {
    Text(
        text = text.uppercase(),
        style = TextStyle(
            fontSize = 11.sp,
            color = theme.primary,
            fontWeight = FontWeight.Bold,
        ),
        maxLines = 1,
        modifier = modifier,
    )
}

/** Hairline divider. */
@Composable
internal fun WidgetDivider(
    theme: WidgetColorScheme,
    modifier: GlanceModifier = GlanceModifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(theme.outlineVariant),
    ) {}
}

/** Grade number colored by threshold (>= 6 passes). */
@Composable
internal fun GradeValue(
    value: Double,
    theme: WidgetColorScheme,
    fontSize: Float = 13f,
    modifier: GlanceModifier = GlanceModifier,
) {
    val color = if (value >= 6.0) theme.pass else theme.fail
    Text(
        text = GradeFormatter.format(value),
        style = TextStyle(fontSize = fontSize.sp, color = color, fontWeight = FontWeight.Bold),
        maxLines = 1,
        modifier = modifier,
    )
}

/** Rounded lesson-hour badge. */
@Composable
internal fun HourBadge(
    hour: Int,
    theme: WidgetColorScheme,
    modifier: GlanceModifier = GlanceModifier,
) {
    Text(
        text = "${hour}ª",
        style = TextStyle(
            fontSize = 12.sp,
            color = theme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        ),
        maxLines = 1,
        modifier = modifier
            .width(34.dp)
            .background(theme.primaryContainer)
            .cornerRadius(8.dp)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

/** Centered empty-state message. */
@Composable
internal fun WidgetEmptyMessage(
    text: String,
    theme: WidgetColorScheme,
    modifier: GlanceModifier = GlanceModifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 12.sp,
                color = theme.secondaryText,
                textAlign = TextAlign.Center,
            ),
            maxLines = 2,
            modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp),
        )
    }
}
