package it.hydr4.argonaut.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CredentialValidatorTest {

    private val validator = CredentialValidator()

    @Test
    fun `valid credentials pass`() {
        val result = validator.validate("ABCDEF", "mario.rossi", "s3cret")
        assertTrue(result.isValid)
        assertEquals(null, result.schoolCodeError)
        assertEquals(null, result.usernameError)
        assertEquals(null, result.passwordError)
    }

    @Test
    fun `blank school code is rejected`() {
        val result = validator.validate("   ", "mario.rossi", "s3cret")
        assertFalse(result.isValid)
        assertEquals(CredentialValidator.FieldError.Required, result.schoolCodeError)
    }

    @Test
    fun `blank username is rejected`() {
        val result = validator.validate("ABCDEF", "", "s3cret")
        assertFalse(result.isValid)
        assertEquals(CredentialValidator.FieldError.Required, result.usernameError)
    }

    @Test
    fun `too-short password is rejected`() {
        val result = validator.validate("ABCDEF", "mario.rossi", "abc")
        assertFalse(result.isValid)
        assertEquals(CredentialValidator.FieldError.TooShort, result.passwordError)
    }

    @Test
    fun `all blank fields are rejected together`() {
        val result = validator.validate("", "", "")
        assertFalse(result.isValid)
        assertEquals(CredentialValidator.FieldError.Required, result.schoolCodeError)
        assertEquals(CredentialValidator.FieldError.Required, result.usernameError)
        assertEquals(CredentialValidator.FieldError.Required, result.passwordError)
    }
}
