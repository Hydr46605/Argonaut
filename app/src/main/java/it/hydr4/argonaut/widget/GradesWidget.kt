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
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import it.hydr4.argonaut.R
import it.hydr4.argonaut.core.util.TimeText
import it.hydr4.argonaut.data.mapping.DashboardMapper
import it.hydr4.argonaut.data.model.VotoItem

/**
 * Tall widget (3×3): the latest grades with subject, date and value, color
 * coded by threshold. Tap opens DidUp (or Argonaut as a fallback).
 */
class GradesWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val theme = WidgetThemeProvider.resolve(context)
        val grades = WidgetData.dashboard(context)?.grades
            ?.filter { it.value != null && it.subjectName != null }
            ?.sortedByDescending { it.datEvento }
            ?.take(6)
            ?.map(DashboardMapper::toVotoItem)
            .orEmpty()

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
                    text = widgetContext.getString(R.string.dashboard_recent_grades),
                    theme = colors,
                )
                if (grades.isEmpty()) {
                    WidgetEmptyMessage(
                        text = widgetContext.getString(R.string.dashboard_no_grades),
                        theme = colors,
                    )
                } else {
                    LazyColumn(modifier = GlanceModifier.fillMaxSize().padding(top = 6.dp)) {
                        items(grades) { voto ->
                            GradesRow(voto = voto, theme = colors)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GradesRow(
    voto: VotoItem,
    theme: WidgetColorScheme,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = voto.subject,
            style = TextStyle(fontSize = 12.sp, color = theme.onCard, fontWeight = FontWeight.Medium),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight(),
        )
        voto.date?.let { date ->
            Text(
                text = TimeText.shortDate(date),
                style = TextStyle(fontSize = 10.sp, color = theme.secondaryText),
                maxLines = 1,
                modifier = GlanceModifier.padding(start = 8.dp),
            )
        }
        GradeValue(
            value = voto.value,
            theme = theme,
            modifier = GlanceModifier.width(34.dp).padding(start = 10.dp),
        )
    }
}

/** Receiver registered in the manifest. */
class GradesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GradesWidget()
}
