package com.aditasha.sepatubersih.domain.model

sealed class Result<out T : Any> {

    data class Success<out T : Any>(val data: Any?) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=${exception.localizedMessage}]"
            is Loading -> "Loading"
        }
    }
}