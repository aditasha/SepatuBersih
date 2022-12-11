package com.aditasha.sepatubersih.presentation.profile

/**
 * Data validation state of the login form.
 */
data class ShoesFormState(
    var nameError: Boolean,
    var brandTypeError: Boolean,
    var colorError: Boolean,
    var isDataValid: Boolean = false
)