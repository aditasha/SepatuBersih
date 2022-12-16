package com.aditasha.sepatubersih.presentation.auth

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditasha.sepatubersih.data.repository.AuthRepositoryImpl
import com.aditasha.sepatubersih.domain.model.Result
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepositoryImpl: AuthRepositoryImpl) :
    ViewModel() {

    private val _authForm = MutableSharedFlow<AuthFormState>()
    val authForm: SharedFlow<AuthFormState> = _authForm

    private val _authResult = MutableSharedFlow<Result<FirebaseUser>>()
    val authResult: SharedFlow<Result<FirebaseUser>> = _authResult

    val currentUser: FirebaseUser?
        get() = authRepositoryImpl.currentUser

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authResult.emit(authRepositoryImpl.login(email, password))
        }
    }

    fun register(email: String, name: String, password: String) {
        viewModelScope.launch {
            _authResult.emit(authRepositoryImpl.register(email, name, password))
        }
    }

    fun editProfile(name: String) {
        viewModelScope.launch {
            _authResult.emit(authRepositoryImpl.editProfile(name))
        }
    }

    fun checkAuthForm(
        email: String,
        name: String? = null,
        number: String? = null,
        password: String
    ) {
        viewModelScope.launch {
            Log.d("test", "name " + name.toString())
            val emailError = emailError(email)
            val nameError = if (name != null) nameError(name) else null
            val numberError = if (number != null) numberError(number) else null
            val passwordError = passwordError(password)
            val authFormState = AuthFormState(emailError, nameError, numberError, passwordError)

            if (number != null && name != null) {
                if (emailError == false && nameError == false && numberError == false && passwordError == false)
                    authFormState.isDataValid = true
            } else
                if (emailError == false && passwordError == false)
                    authFormState.isDataValid = true

            _authForm.emit(authFormState)
        }
    }

    fun checkProfileEditForm(name: String, number: String) {
        viewModelScope.launch {
            val nameError = nameError(name)
            val numberError = numberError(number)
            val authFormState = AuthFormState(nameError = nameError, numberError = numberError)

            if (nameError == false && numberError == false)
                authFormState.isDataValid = true

            _authForm.emit(authFormState)
        }
    }

    private fun emailError(email: String): Boolean? {
        return if (email.isBlank()) null
        else !Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun nameError(name: String): Boolean? {
        Log.d("test", "name " + name + "length " + name.length)
        return if (name.isBlank()) null
        else name.length < 3
    }

    private fun numberError(number: String): Boolean? {
        val a = !Patterns.PHONE.matcher(number).matches()
        Log.d("test", "result " + a + " " + number)
        return if (number.isBlank()) null
        else !Patterns.PHONE.matcher(number).matches()
    }

    private fun passwordError(password: String): Boolean? {
        return if (password.isBlank()) null
        else password.length < 6
    }

    fun logout() {
        authRepositoryImpl.logout()
    }
}