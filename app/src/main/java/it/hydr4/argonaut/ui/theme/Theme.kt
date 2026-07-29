@file:Suppress("FunctionNaming") // ArgonautTheme composable is PascalCase by convention.

package it.hydr4.argonaut.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val ArgonautLightColorScheme = lightColorScheme(
    primary = ArgonautBlue,
    onPrimary = ArgonautOnBlue,
    primaryContainer = ArgonautBlueContainer,
    onPrimaryContainer = ArgonautOnBlueContainer,
    secondary = ArgonautSteel,
    onSecondary = ArgonautOnSteel,
    secondaryContainer = ArgonautSteelContainer,
    onSecondaryContainer = ArgonautOnSteelContainer,
    tertiary = ArgonautLilac,
    onTertiary = ArgonautOnLilac,
    tertiaryContainer = ArgonautLilac.copy(alpha = 0.25f),
    onTertiaryContainer = ArgonautLilac,
    error = ArgonautErrorLight,
    onError = ArgonautOnErrorLight,
    background = ArgonautPaper,
    onBackground = ArgonautOnPaper,
    surface = ArgonautPaper,
    onSurface = ArgonautOnPaper,
    surfaceVariant = ArgonautSurfaceVariantLight,
    onSurfaceVariant = ArgonautOnSurfaceVariantLight,
    outline = ArgonautOutlineLight,
    outlineVariant = ArgonautOutlineVariantLight,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = ArgonautSurfaceContainerLowLight,
    surfaceContainer = ArgonautSurfaceContainerLight,
    surfaceContainerHigh = ArgonautSurfaceContainerHighLight,
    surfaceContainerHighest = ArgonautSurfaceContainerHighestLight,
)

private val ArgonautDarkColorScheme = darkColorScheme(
    primary = ArgonautBlueDark,
    onPrimary = ArgonautOnBlueDark,
    primaryContainer = ArgonautBlueContainerDark,
    onPrimaryContainer = ArgonautOnBlueContainerDark,
    secondary = ArgonautSteelDark,
    onSecondary = ArgonautOnSteelDark,
    secondaryContainer = ArgonautSteelContainerDark,
    onSecondaryContainer = ArgonautOnSteelContainerDark,
    tertiary = ArgonautLilacDark,
    onTertiary = ArgonautOnLilacDark,
    tertiaryContainer = ArgonautLilacDark.copy(alpha = 0.2f),
    onTertiaryContainer = ArgonautLilacDark,
    error = ArgonautErrorDark,
    onError = ArgonautOnErrorDark,
    background = ArgonautInk,
    onBackground = ArgonautOnInk,
    surface = ArgonautSurfaceDark,
    onSurface = ArgonautOnInk,
    surfaceVariant = ArgonautSurfaceVariantDark,
    onSurfaceVariant = ArgonautOnSurfaceVariantDark,
    outline = ArgonautOutlineDark,
    outlineVariant = ArgonautOutlineVariantDark,
    surfaceContainerLowest = Color(0xFF0B0D11),
    surfaceContainerLow = ArgonautSurfaceContainerLowDark,
    surfaceContainer = ArgonautSurfaceContainerDark,
    surfaceContainerHigh = ArgonautSurfaceContainerHighDark,
    surfaceContainerHighest = ArgonautSurfaceContainerHighestDark,
)

/**
 * Theme entry point. [darkTheme] is resolved by the caller (system or explicit
 * override); [dynamicColor] is an opt-in preference honored on Android 12+.
 */
@Composable
fun ArgonautTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ArgonautDarkColorScheme
        else -> ArgonautLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ArgonautTypography,
        shapes = ArgonautShapes,
        content = content,
    )
}

/**
 * Grade-consistent colors derived from the active [MaterialTheme].
 *
 * Exposed as an immutable snapshot so list items, chips and widgets agree on
 * what a passing or failing grade looks like.
 */
@Immutable
data class GradeColors(
    val pass: Color,
    val onPass: Color,
    val passContainer: Color,
    val onPassContainer: Color,
    val fail: Color,
    val onFail: Color,
    val failContainer: Color,
    val onFailContainer: Color,
) {
    companion object {
        @Composable
        fun current(): GradeColors {
            val dark = isSystemInDarkTheme()
            return if (dark) {
                GradeColors(
                    pass = ArgonautPassDark,
                    onPass = ArgonautOnInk,
                    passContainer = ArgonautPassContainerDark,
                    onPassContainer = ArgonautPassDark,
                    fail = ArgonautFailDark,
                    onFail = ArgonautOnInk,
                    failContainer = ArgonautFailContainerDark,
                    onFailContainer = ArgonautFailDark,
                )
            } else {
                GradeColors(
                    pass = ArgonautPassLight,
                    onPass = ArgonautOnErrorLight,
                    passContainer = ArgonautPassContainerLight,
                    onPassContainer = ArgonautPassLight,
                    fail = ArgonautFailLight,
                    onFail = ArgonautOnErrorLight,
                    failContainer = ArgonautFailContainerLight,
                    onFailContainer = ArgonautFailLight,
                )
            }
        }
    }
}
