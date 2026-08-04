@file:Suppress("FunctionNaming") // Compose navigation composable is PascalCase by convention.

package it.hydr4.argonaut.ui.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.hydr4.argonaut.data.SessionState
import it.hydr4.argonaut.ui.screens.about.AboutScreen
import it.hydr4.argonaut.ui.screens.dashboard.DashboardScreen
import it.hydr4.argonaut.ui.screens.login.LoginScreen
import it.hydr4.argonaut.ui.screens.settings.SettingsScreen

/**
 * The whole navigation graph. The graph is keyed by [session]: on a login or
 * logout the graph is rebuilt with the correct start destination, so the back
 * stack can never leak a stale login screen.
 */
@Composable
fun ArgonautNavHost(
    session: SessionState,
    windowSizeClass: WindowSizeClass?,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val startDestination = if (session is SessionState.Authenticated) Routes.DASHBOARD else Routes.LOGIN

    key(session) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier,
        ) {
            composable(Routes.LOGIN) {
                LoginScreen()
            }
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    session = session as? SessionState.Authenticated,
                    windowSizeClass = windowSizeClass,
                    onOpenSettings = {
                        navController.navigate(Routes.SETTINGS)
                    },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenAbout = {
                        navController.navigate(Routes.ABOUT)
                    },
                )
            }
            composable(Routes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
