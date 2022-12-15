package com.aditasha.sepatubersih.presentation.admin

/**
 * Data validation state of the login form.
 */
data class ArticleFormState(
    var nameError: Boolean? = null,
    var descError: Boolean? = null,
    var linkError: Boolean? = null,
    var imageError: Boolean? = null,
    var isDataValid: Boolean = false
)