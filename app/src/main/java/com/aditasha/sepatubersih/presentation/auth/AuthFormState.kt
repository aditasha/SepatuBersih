package com.aditasha.sepatubersih.presentation.auth

/**
 * Data validation state of the login form.
 */
data class AuthFormState(
    var emailError: Boolean? = null,
    var nameError: Boolean? = null,
    var numberError: Boolean? = null,
    var passwordError: Boolean? = null,
    var isDataValid: Boolean = false
)