package it.hydr4.argonaut.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class GradeFormatterTest {

    @Test
    fun `integer values render without decimals`() {
        assertEquals("10", GradeFormatter.format(10.0))
        assertEquals("7", GradeFormatter.format(7.0))
        assertEquals("6", GradeFormatter.format(6.0))
    }

    @Test
    fun `fractional values use the Italian decimal comma`() {
        assertEquals("7,5", GradeFormatter.format(7.5))
        assertEquals("8,4", GradeFormatter.format(8.4))
        assertEquals("6,3", GradeFormatter.format(6.25))
    }

    @Test
    fun `null renders as an em dash`() {
        assertEquals("—", GradeFormatter.formatOrDash(null))
    }

    @Test
    fun `values round to one decimal`() {
        assertEquals("7,7", GradeFormatter.format(7.66))
    }
}
