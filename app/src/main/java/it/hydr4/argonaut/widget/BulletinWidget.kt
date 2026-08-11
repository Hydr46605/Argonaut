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
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import it.hydr4.argonaut.R
import it.hydr4.argonaut.core.util.TimeText
import it.hydr4.argonaut.data.mapping.DashboardMapper
import it.hydr4.argonaut.data.model.BulletinItem

/**
 * Tertiary widget (tall, 3×4): the latest teacher-bulletin ("bacheca") entries
 * in a scrollable list. Tap opens DidUp (or Argonaut as a fallback).
 */
class BulletinWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val theme = WidgetThemeProvider.resolve(context)
        val entries = WidgetData.bulletins(context).map(DashboardMapper::toBulletinItem)

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
                    text = widgetContext.getString(R.string.widget_bulletin_heading),
                    theme = colors,
                )
                if (entries.isEmpty()) {
                    WidgetEmptyMessage(
                        text = widgetContext.getString(R.string.widget_no_data),
                        theme = colors,
                    )
                } else {
                    LazyColumn(modifier = GlanceModifier.fillMaxSize().padding(top = 6.dp)) {
                        items(entries) { entry ->
                            BulletinRow(entry = entry, theme = colors)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BulletinRow(
    entry: BulletinItem,
    theme: WidgetColorScheme,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
    ) {
        Text(
            text = entry.title,
            style = TextStyle(fontSize = 13.sp, color = theme.onCard, fontWeight = FontWeight.Medium),
            maxLines = 2,
        )
        val meta = buildList {
            entry.date?.let { TimeText.wireDateOrRaw(it) }?.let(::add)
            entry.author?.let(::add)
        }
        if (meta.isNotEmpty()) {
            Text(
                text = meta.joinToString(" · "),
                style = TextStyle(fontSize = 11.sp, color = theme.secondaryText),
                maxLines = 1,
                modifier = GlanceModifier.padding(top = 1.dp),
            )
        }
        WidgetDivider(theme = theme, modifier = GlanceModifier.padding(top = 5.dp))
    }
}

/** Receiver registered in the manifest. */
class BulletinWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BulletinWidget()
}
