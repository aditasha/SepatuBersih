package com.aditasha.sepatubersih.presentation.profile

/**
 * Data validation state of the login form.
 */
data class ShoesFormState(
    var nameError: Boolean? = null,
    var brandTypeError: Boolean? = null,
    var colorError: Boolean? = null,
    var isDataValid: Boolean = false
)