package com.aditasha.sepatubersih.domain.repository

import com.aditasha.sepatubersih.domain.model.Result
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun register(email: String, name: String, password: String): Result<FirebaseUser>
    suspend fun login(email: String, password: String): Result<FirebaseUser>
    suspend fun editProfile(name: String): Result<FirebaseUser>
    fun logout()
}