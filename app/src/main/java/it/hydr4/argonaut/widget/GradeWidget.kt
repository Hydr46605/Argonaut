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
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import it.hydr4.argo.models.Dashboard
import it.hydr4.argonaut.R
import it.hydr4.argonaut.core.util.GradeFormatter
import it.hydr4.argonaut.data.mapping.DashboardMapper
import it.hydr4.argonaut.data.model.VotoItem

/**
 * Primary widget (medium, 3×2): the `mediaGenerale` with a large typographic
 * treatment, the student's class and name, and a mini list of the last three
 * grades. Themed through [WidgetThemeProvider] so it matches the app; tapping
 * it opens the official DidUp app (or Argonaut when DidUp is not installed).
 */
class GradeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val theme = WidgetThemeProvider.resolve(context)
        val dashboard = WidgetData.dashboard(context)
        val recentGrades = dashboard?.grades
            ?.filter { it.value != null && it.subjectName != null }
            ?.sortedByDescending { it.datEvento }
            ?.take(3)
            ?.map(DashboardMapper::toVotoItem)
            .orEmpty()

        provideContent {
            GradeWidgetContent(
                theme = theme,
                dashboard = dashboard,
                recentGrades = recentGrades,
            )
        }
    }
}

/** "Name · Class" line from the cached profile, or empty when logged out. */
private fun studentLine(context: Context): String {
    val profile = WidgetData.profile(context) ?: return ""
    val name = profile.alunno.nominativo.ifBlank { profile.alunno.nome }
    val clazz = profile.scheda.classe.denomination
    return listOf(name, clazz).filter { it.isNotBlank() }.joinToString(" · ")
}

/** Inner layout: average header plus the mini grade list. */
@Composable
private fun GradeWidgetContent(
    theme: WidgetTheme,
    dashboard: Dashboard?,
    recentGrades: List<VotoItem>,
) {
    val widgetContext = LocalContext.current
    val colors = theme.colors
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clickable(actionStartActivity(WidgetLaunch.intent(widgetContext))),
    ) {
        WidgetHeader(
            text = widgetContext.getString(R.string.dashboard_media_generale),
            theme = colors,
        )
        Text(
            text = GradeFormatter.formatOrDash(dashboard?.overallAverage),
            style = TextStyle(fontSize = 40.sp, color = colors.primary, fontWeight = FontWeight.Bold),
            maxLines = 1,
            modifier = GlanceModifier.padding(top = 2.dp),
        )
        if (theme.showStudentName) {
            val identity = studentLine(widgetContext)
            if (identity.isNotBlank()) {
                Text(
                    text = identity,
                    style = TextStyle(fontSize = 11.sp, color = colors.secondaryText),
                    maxLines = 1,
                )
            }
        }
        WidgetDivider(
            theme = colors,
            modifier = GlanceModifier.padding(top = 6.dp, bottom = 4.dp),
        )
        if (recentGrades.isEmpty()) {
            WidgetEmptyMessage(
                text = widgetContext.getString(R.string.widget_no_data),
                theme = colors,
            )
        } else {
            RecentGradesList(grades = recentGrades, theme = colors)
        }
    }
}

/** The three most recent grades, subject on the left and value on the right. */
@Composable
private fun RecentGradesList(
    grades: List<VotoItem>,
    theme: WidgetColorScheme,
) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        grades.forEach { voto ->
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = voto.subject,
                    style = TextStyle(fontSize = 12.sp, color = theme.onCard),
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight(),
                )
                GradeValue(
                    value = voto.value,
                    theme = theme,
                    modifier = GlanceModifier.width(34.dp),
                )
            }
        }
    }
}

/** Receiver registered in the manifest. */
class GradeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GradeWidget()
}
