package com.aditasha.sepatubersih.data.repository

import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val firebaseAuth: FirebaseAuth) :
    AuthRepository {

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return try {
            Result.Loading
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val profileBuilder =
                UserProfileChangeRequest.Builder().setDisplayName(email.substringBefore("@"))
                    .build()
            result.user!!.updateProfile(profileBuilder).await()
            Result.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            Result.Loading
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }

    override suspend fun editProfile(name: String): Result<FirebaseUser> {
        return try {
            Result.Loading
            val profileBuilder = UserProfileChangeRequest.Builder().setDisplayName(name).build()
            currentUser?.updateProfile(profileBuilder)?.await()
            Result.Success(currentUser)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }
}