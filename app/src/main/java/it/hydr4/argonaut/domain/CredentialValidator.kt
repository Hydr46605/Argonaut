package it.hydr4.argonaut.domain

import javax.inject.Inject

/**
 * Validates the login form before any network traffic happens. Field-level
 * errors drive inline supporting text and error states on the inputs.
 */
class CredentialValidator @Inject constructor() {

    sealed interface FieldError {
        data object Required : FieldError
        data object TooShort : FieldError
    }

    data class ValidationResult(
        val schoolCodeError: FieldError? = null,
        val usernameError: FieldError? = null,
        val passwordError: FieldError? = null,
    ) {
        val isValid: Boolean
            get() = schoolCodeError == null && usernameError == null && passwordError == null
    }

    fun validate(schoolCode: String, username: String, password: String): ValidationResult = ValidationResult(
        schoolCodeError = when {
            schoolCode.isBlank() -> FieldError.Required
            else -> null
        },
        usernameError = when {
            username.isBlank() -> FieldError.Required
            else -> null
        },
        passwordError = when {
            password.isBlank() -> FieldError.Required
            password.length < MIN_PASSWORD_LENGTH -> FieldError.TooShort
            else -> null
        },
    )

    private companion object {
        const val MIN_PASSWORD_LENGTH = 4
    }
}
