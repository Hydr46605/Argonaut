@file:Suppress("FunctionNaming") // Glance composables are PascalCase by convention.

package it.hydr4.argonaut.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import it.hydr4.argonaut.R
import it.hydr4.argonaut.data.mapping.DashboardMapper
import it.hydr4.argonaut.data.model.ScheduleSlotItem

/**
 * Secondary widget (small, 2×2): today's first three timetable entries with
 * hour and subject, glanceable from the home screen. Tap opens DidUp (or
 * Argonaut as a fallback).
 */
class ScheduleWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val theme = WidgetThemeProvider.resolve(context)
        val slots = WidgetData.schedule(context)
            .sortedBy { it.hourNumber ?: Int.MAX_VALUE }
            .take(3)
            .map(DashboardMapper::toScheduleSlot)

        provideContent {
            val widgetContext = LocalContext.current
            val colors = theme.colors
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(colors.background)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .clickable(actionStartActivity(WidgetLaunch.intent(widgetContext))),
            ) {
                WidgetHeader(
                    text = widgetContext.getString(R.string.dashboard_schedule),
                    theme = colors,
                )
                if (slots.isEmpty()) {
                    WidgetEmptyMessage(
                        text = widgetContext.getString(R.string.dashboard_schedule_empty),
                        theme = colors,
                    )
                } else {
                    Column(modifier = GlanceModifier.fillMaxWidth().padding(top = 8.dp)) {
                        slots.forEach { slot ->
                            ScheduleRow(slot = slot, theme = colors)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleRow(
    slot: ScheduleSlotItem,
    theme: WidgetColorScheme,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().height(26.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HourBadge(hour = slot.hour, theme = theme)
        Text(
            text = slot.subject.ifBlank { "—" },
            style = TextStyle(fontSize = 12.sp, color = theme.onCard, fontWeight = FontWeight.Medium),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight().padding(start = 8.dp),
        )
    }
}

/** Receiver registered in the manifest. */
class ScheduleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScheduleWidget()
}
