@file:Suppress("FunctionNaming", "LongMethod") // Compose screen composables are PascalCase and layout-heavy.

package it.hydr4.argonaut.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.hydr4.argonaut.R
import it.hydr4.argonaut.data.DarkModePreference

/**
 * Settings: Material 3 preference-style rows — switches for dynamic color and
 * dark mode, a refresh-frequency dropdown for the widgets, the about entry and
 * the logout action guarded by a confirmation dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            AppearanceSection(
                dynamicColor = state.preferences.dynamicColor,
                darkMode = state.preferences.darkMode,
                onDynamicColorChange = viewModel::setDynamicColor,
                onDarkModeChange = viewModel::setDarkMode,
            )
            WidgetSection(
                refreshMinutes = state.preferences.widgetRefreshMinutes,
                showStudentName = state.preferences.showStudentName,
                onRefreshMinutesChange = viewModel::setWidgetRefreshMinutes,
                onShowStudentNameChange = viewModel::setShowStudentName,
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_about)) },
                leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) },
                trailingContent = { Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onOpenAbout),
            )
            Spacer(modifier = Modifier.height(16.dp))
            state.loggedInAs?.let { name ->
                Text(
                    text = stringResource(R.string.settings_logged_in_as, name),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            ListItem(
                headlineContent = {
                    Text(stringResource(R.string.settings_logout), color = MaterialTheme.colorScheme.error)
                },
                leadingContent = {
                    Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.clickable { showLogoutDialog = true },
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.settings_logout_confirm_title)) },
            text = { Text(stringResource(R.string.settings_logout_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                ) {
                    Text(stringResource(R.string.settings_logout_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.settings_logout_cancel))
                }
            },
        )
    }
}

@Composable
private fun AppearanceSection(
    dynamicColor: Boolean,
    darkMode: DarkModePreference,
    onDynamicColorChange: (Boolean) -> Unit,
    onDarkModeChange: (DarkModePreference) -> Unit,
) {
    Text(
        text = stringResource(R.string.settings_appearance).uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
    )
    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_dynamic_color)) },
        supportingContent = { Text(stringResource(R.string.settings_dynamic_color_summary)) },
        leadingContent = { Icon(Icons.Outlined.Palette, contentDescription = null) },
        trailingContent = {
            Switch(checked = dynamicColor, onCheckedChange = onDynamicColorChange)
        },
    )
    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_dark_mode)) },
        leadingContent = { Icon(Icons.Outlined.Palette, contentDescription = null) },
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DarkModePreference.entries.forEach { mode ->
            FilterChip(
                selected = darkMode == mode,
                onClick = { onDarkModeChange(mode) },
                label = { Text(stringResource(mode.toLabelRes())) },
            )
        }
    }
}

@Composable
private fun WidgetSection(
    refreshMinutes: Int,
    showStudentName: Boolean,
    onRefreshMinutesChange: (Int) -> Unit,
    onShowStudentNameChange: (Boolean) -> Unit,
) {
    Text(
        text = stringResource(R.string.settings_widgets).uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 4.dp),
    )
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    Box {
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_widget_refresh)) },
            supportingContent = { Text(stringResource(R.string.settings_widget_refresh_summary)) },
            leadingContent = { Icon(Icons.Outlined.Widgets, contentDescription = null) },
            trailingContent = {
                Text(
                    text = refreshMinutes.toRefreshLabel(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            modifier = Modifier.clickable { menuExpanded = true },
        )
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            val options = listOf(15, 30, 60, 180)
            options.forEach { minutes ->
                DropdownMenuItem(
                    text = { Text(minutes.toRefreshLabel()) },
                    onClick = {
                        menuExpanded = false
                        onRefreshMinutesChange(minutes)
                    },
                )
            }
        }
    }
    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_widget_student_name)) },
        supportingContent = { Text(stringResource(R.string.settings_widget_student_name_summary)) },
        leadingContent = { Icon(Icons.Outlined.Badge, contentDescription = null) },
        trailingContent = {
            Switch(checked = showStudentName, onCheckedChange = onShowStudentNameChange)
        },
    )
}

@Composable
private fun Int.toRefreshLabel(): String = when (this) {
    15 -> stringResource(R.string.settings_widget_refresh_15)
    30 -> stringResource(R.string.settings_widget_refresh_30)
    60 -> stringResource(R.string.settings_widget_refresh_60)
    180 -> stringResource(R.string.settings_widget_refresh_180)
    else -> "$this min"
}

private fun DarkModePreference.toLabelRes(): Int = when (this) {
    DarkModePreference.SYSTEM -> R.string.settings_dark_mode_system
    DarkModePreference.LIGHT -> R.string.settings_dark_mode_light
    DarkModePreference.DARK -> R.string.settings_dark_mode_dark
}
