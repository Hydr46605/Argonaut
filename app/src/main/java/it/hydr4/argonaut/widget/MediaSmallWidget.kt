@file:Suppress("FunctionNaming") // Glance composables are PascalCase by convention.

package it.hydr4.argonaut.widget

import android.content.Context
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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import it.hydr4.argonaut.R
import it.hydr4.argonaut.core.util.GradeFormatter

/**
 * Compact widget (2×1): the `mediaGenerale` with the student name — the most
 * glanceable number in the whole app, at a glance. Tap opens DidUp (or
 * Argonaut as a fallback).
 */
class MediaSmallWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val theme = WidgetThemeProvider.resolve(context)
        val dashboard = WidgetData.dashboard(context)

        provideContent {
            val widgetContext = LocalContext.current
            val colors = theme.colors
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(colors.background)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .clickable(actionStartActivity(WidgetLaunch.intent(widgetContext))),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = widgetContext.getString(R.string.dashboard_media_generale).uppercase(),
                    style = TextStyle(fontSize = 11.sp, color = colors.primary, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                )
                Text(
                    text = GradeFormatter.formatOrDash(dashboard?.overallAverage),
                    style = TextStyle(fontSize = 28.sp, color = colors.primary, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                )
            }
        }
    }
}

/** Receiver registered in the manifest. */
class MediaSmallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediaSmallWidget()
}
