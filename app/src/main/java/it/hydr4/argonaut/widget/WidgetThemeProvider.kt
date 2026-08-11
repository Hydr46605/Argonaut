package it.hydr4.argonaut.widget

import android.content.Context
import android.content.res.Configuration
import androidx.glance.unit.ColorProvider
import it.hydr4.argonaut.data.DarkModePreference
import it.hydr4.argonaut.data.SharedPreferencesSettingsRepository
import it.hydr4.argonaut.ui.theme.ArgonautBlue
import it.hydr4.argonaut.ui.theme.ArgonautBlueContainer
import it.hydr4.argonaut.ui.theme.ArgonautBlueContainerDark
import it.hydr4.argonaut.ui.theme.ArgonautBlueDark
import it.hydr4.argonaut.ui.theme.ArgonautFailDark
import it.hydr4.argonaut.ui.theme.ArgonautFailLight
import it.hydr4.argonaut.ui.theme.ArgonautInk
import it.hydr4.argonaut.ui.theme.ArgonautOnBlue
import it.hydr4.argonaut.ui.theme.ArgonautOnBlueContainer
import it.hydr4.argonaut.ui.theme.ArgonautOnBlueContainerDark
import it.hydr4.argonaut.ui.theme.ArgonautOnBlueDark
import it.hydr4.argonaut.ui.theme.ArgonautOnInk
import it.hydr4.argonaut.ui.theme.ArgonautOnPaper
import it.hydr4.argonaut.ui.theme.ArgonautOnSurfaceVariantDark
import it.hydr4.argonaut.ui.theme.ArgonautOnSurfaceVariantLight
import it.hydr4.argonaut.ui.theme.ArgonautOutlineDark
import it.hydr4.argonaut.ui.theme.ArgonautOutlineLight
import it.hydr4.argonaut.ui.theme.ArgonautOutlineVariantDark
import it.hydr4.argonaut.ui.theme.ArgonautOutlineVariantLight
import it.hydr4.argonaut.ui.theme.ArgonautPaper
import it.hydr4.argonaut.ui.theme.ArgonautPassDark
import it.hydr4.argonaut.ui.theme.ArgonautPassLight
import it.hydr4.argonaut.ui.theme.ArgonautSurfaceContainerDark
import it.hydr4.argonaut.ui.theme.ArgonautSurfaceContainerHighestDark
import it.hydr4.argonaut.ui.theme.ArgonautSurfaceContainerHighestLight
import it.hydr4.argonaut.ui.theme.ArgonautSurfaceContainerLight

/**
 * Widget color tokens: a small, explicit subset of the Material 3 scheme that
 * Glance renders natively (no compose runtime involved).
 */
data class WidgetColorScheme(
    val background: ColorProvider,
    val onBackground: ColorProvider,
    val card: ColorProvider,
    val onCard: ColorProvider,
    val primary: ColorProvider,
    val onPrimary: ColorProvider,
    val primaryContainer: ColorProvider,
    val onPrimaryContainer: ColorProvider,
    val secondaryText: ColorProvider,
    val outline: ColorProvider,
    val outlineVariant: ColorProvider,
    val surfaceContainerHighest: ColorProvider,
    val pass: ColorProvider,
    val fail: ColorProvider,
)

/** Everything a widget needs to render: palette plus widget preferences. */
data class WidgetTheme(
    val colors: WidgetColorScheme,
    val showStudentName: Boolean,
)

/**
 * Resolves the app's palette and widget preferences for Glance compositions.
 * Reads the same [SharedPreferencesSettingsRepository] the app uses, honoring
 * the dark-mode override; the curated Argonaut palette keeps widgets cohesive
 * with the app identity regardless of dynamic colors.
 */
object WidgetThemeProvider {

    fun resolve(context: Context): WidgetTheme {
        val preferences = SharedPreferencesSettingsRepository(context).preferences.value
        val dark = when (preferences.darkMode) {
            DarkModePreference.SYSTEM -> isSystemDark(context)
            DarkModePreference.LIGHT -> false
            DarkModePreference.DARK -> true
        }
        return WidgetTheme(
            colors = schemeFor(dark),
            showStudentName = preferences.showStudentName,
        )
    }

    /** Pure palette resolution; unit-tested without Android. */
    fun schemeFor(dark: Boolean): WidgetColorScheme = if (dark) darkScheme() else lightScheme()

    private fun isSystemDark(context: Context): Boolean {
        val mode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun lightScheme(): WidgetColorScheme = WidgetColorScheme(
        background = ArgonautPaper.toColorProvider(),
        onBackground = ArgonautOnPaper.toColorProvider(),
        card = ArgonautSurfaceContainerLight.toColorProvider(),
        onCard = ArgonautOnPaper.toColorProvider(),
        primary = ArgonautBlue.toColorProvider(),
        onPrimary = ArgonautOnBlue.toColorProvider(),
        primaryContainer = ArgonautBlueContainer.toColorProvider(),
        onPrimaryContainer = ArgonautOnBlueContainer.toColorProvider(),
        secondaryText = ArgonautOnSurfaceVariantLight.toColorProvider(),
        outline = ArgonautOutlineLight.toColorProvider(),
        outlineVariant = ArgonautOutlineVariantLight.toColorProvider(),
        surfaceContainerHighest = ArgonautSurfaceContainerHighestLight.toColorProvider(),
        pass = ArgonautPassLight.toColorProvider(),
        fail = ArgonautFailLight.toColorProvider(),
    )

    private fun darkScheme(): WidgetColorScheme = WidgetColorScheme(
        background = ArgonautInk.toColorProvider(),
        onBackground = ArgonautOnInk.toColorProvider(),
        card = ArgonautSurfaceContainerDark.toColorProvider(),
        onCard = ArgonautOnInk.toColorProvider(),
        primary = ArgonautBlueDark.toColorProvider(),
        onPrimary = ArgonautOnBlueDark.toColorProvider(),
        primaryContainer = ArgonautBlueContainerDark.toColorProvider(),
        onPrimaryContainer = ArgonautOnBlueContainerDark.toColorProvider(),
        secondaryText = ArgonautOnSurfaceVariantDark.toColorProvider(),
        outline = ArgonautOutlineDark.toColorProvider(),
        outlineVariant = ArgonautOutlineVariantDark.toColorProvider(),
        surfaceContainerHighest = ArgonautSurfaceContainerHighestDark.toColorProvider(),
        pass = ArgonautPassDark.toColorProvider(),
        fail = ArgonautFailDark.toColorProvider(),
    )
}

internal fun androidx.compose.ui.graphics.Color.toColorProvider(): ColorProvider = ColorProvider(this)
