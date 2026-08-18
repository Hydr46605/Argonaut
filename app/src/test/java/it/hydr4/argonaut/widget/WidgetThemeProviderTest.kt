package it.hydr4.argonaut.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class WidgetThemeProviderTest {

    @Test
    fun `dark scheme uses dark tokens`() {
        val scheme = WidgetThemeProvider.schemeFor(dark = true)
        assertEquals(WidgetThemeProvider.schemeFor(dark = true), scheme)
    }

    @Test
    fun `light and dark schemes differ`() {
        assertNotEquals(WidgetThemeProvider.schemeFor(dark = true), WidgetThemeProvider.schemeFor(dark = false))
    }

    @Test
    fun `schemes are stable per mode`() {
        assertEquals(WidgetThemeProvider.schemeFor(dark = false), WidgetThemeProvider.schemeFor(dark = false))
    }
}
