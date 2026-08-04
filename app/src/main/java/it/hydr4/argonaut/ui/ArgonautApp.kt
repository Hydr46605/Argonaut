@file:Suppress("FunctionNaming") // Compose root composables are PascalCase by convention.

package it.hydr4.argonaut.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.hydr4.argonaut.data.DarkModePreference
import it.hydr4.argonaut.data.SessionState
import it.hydr4.argonaut.ui.navigation.ArgonautNavHost
import it.hydr4.argonaut.ui.theme.ArgonautTheme

/**
 * Composition root: resolves the theme from preferences, computes the window
 * size class once, and switches between the splash and the navigation graph
 * based on the restored session.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ArgonautApp(viewModel: AppViewModel = hiltViewModel()) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    val darkTheme = when (preferences.darkMode) {
        DarkModePreference.SYSTEM -> isSystemInDarkTheme()
        DarkModePreference.LIGHT -> false
        DarkModePreference.DARK -> true
    }
    val windowSizeClass: WindowSizeClass? = LocalActivity.current?.let { activity -> calculateWindowSizeClass(activity) }

    ArgonautTheme(
        darkTheme = darkTheme,
        dynamicColor = preferences.dynamicColor,
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (session) {
                SessionState.Restoring -> RestoringSplash()
                else -> ArgonautNavHost(session = session, windowSizeClass = windowSizeClass)
            }
        }
    }
}

@Composable
private fun RestoringSplash() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
