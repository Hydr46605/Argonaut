@file:Suppress("FunctionNaming", "LongMethod") // Compose screen composables are PascalCase and layout-heavy.

package it.hydr4.argonaut.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.hydr4.argonaut.R
import it.hydr4.argonaut.data.RefreshFailure
import it.hydr4.argonaut.data.SessionState
import it.hydr4.argonaut.data.model.DashboardSummary
import it.hydr4.argonaut.ui.components.AverageCard
import it.hydr4.argonaut.ui.components.EmptyState
import it.hydr4.argonaut.ui.components.ErrorState
import it.hydr4.argonaut.ui.components.LoadingState
import it.hydr4.argonaut.ui.components.ScheduleSlotRow
import it.hydr4.argonaut.ui.components.SectionHeader
import it.hydr4.argonaut.ui.components.VotoListItem

/**
 * The heart of Argonaut: the general average as a large animated hero, recent
 * grades, the daily schedule and an absences/reminders glance — arranged in a
 * responsive grid that collapses to a single column on phones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    session: SessionState.Authenticated?,
    windowSizeClass: WindowSizeClass?,
    onOpenSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Outlined.Sync, contentDescription = stringResource(R.string.dashboard_refresh))
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when (val current = state) {
            DashboardUiState.Loading -> LoadingState(modifier = Modifier.padding(padding))
            is DashboardUiState.Error -> ErrorState(
                title = stringResource(current.failure.toTitleRes()),
                onRetry = viewModel::refresh,
                modifier = Modifier.padding(padding),
            )
            is DashboardUiState.Success -> DashboardContent(
                state = current,
                studentName = session?.studentName,
                className = session?.className,
                windowSizeClass = windowSizeClass,
                onRefresh = viewModel::refresh,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Success,
    studentName: String?,
    className: String?,
    windowSizeClass: WindowSizeClass?,
    onRefresh: () -> Unit,
    contentPadding: PaddingValues,
) {
    val wideLayout = windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyVerticalGrid(
            columns = if (wideLayout) GridCells.Fixed(2) else GridCells.Fixed(1),
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                AverageCard(
                    average = state.summary.overallAverage,
                    studentName = studentName,
                    className = className,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            stickyHeader(key = "header-grades") {
                SectionHeader(title = stringResource(R.string.dashboard_recent_grades))
            }
            if (state.summary.recentGrades.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(text = stringResource(R.string.dashboard_no_grades))
                }
            } else {
                // Keys must be globally unique across the whole grid: grades can
                // share subject/value/date (e.g. two 8.0 in the same subject on
                // the same day), so the section-prefixed index is the identity.
                itemsIndexed(
                    items = state.summary.recentGrades,
                    key = { index, _ -> "grade-$index" },
                ) { _, voto ->
                    VotoListItem(
                        voto = voto,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            stickyHeader(key = "header-schedule") {
                SectionHeader(title = stringResource(R.string.dashboard_schedule))
            }
            if (state.schedule.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        text = stringResource(R.string.dashboard_schedule_empty),
                        icon = Icons.Outlined.WbSunny,
                    )
                }
            } else {
                itemsIndexed(
                    items = state.schedule,
                    key = { index, _ -> "slot-$index" },
                ) { _, slot ->
                    ScheduleSlotRow(
                        slot = slot,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            stickyHeader(key = "header-absences") {
                SectionHeader(title = stringResource(R.string.dashboard_absences))
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                AbsenceSummaryCard(
                    summary = state.summary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/** Glanceable absences + reminders card. */
@Composable
internal fun AbsenceSummaryCard(
    summary: DashboardSummary,
    modifier: Modifier = Modifier,
) {
    val justifiableCount = summary.absences.count { it.justifiable && !it.justified }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.EventBusy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = pluralStringResource(R.plurals.dashboard_absences_count, justifiableCount, justifiableCount),
                    style = MaterialTheme.typography.titleSmall,
                )
                if (summary.reminders.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = pluralStringResource(R.plurals.dashboard_reminders_count, summary.reminders.size, summary.reminders.size),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (justifiableCount == 0 && summary.reminders.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.dashboard_absences_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun RefreshFailure.toTitleRes(): Int = when (this) {
    RefreshFailure.SESSION_EXPIRED -> R.string.dashboard_error_session
    RefreshFailure.NETWORK -> R.string.dashboard_error_network
    RefreshFailure.SERVER -> R.string.dashboard_error_server
}
