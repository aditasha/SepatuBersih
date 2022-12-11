package com.aditasha.sepatubersih.data.repository

import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbAddress
import com.aditasha.sepatubersih.domain.model.SbShoes
import com.aditasha.sepatubersih.domain.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth
) : ProfileRepository {

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun addAddress(sbAddress: SbAddress): Result<Any> {
        return try {
            Result.Loading
            val user = firebaseAuth.currentUser
            val uid = user?.uid
            val addressRef = firebaseDatabase.reference.child(RealtimeDatabaseConstants.ADDRESS)
                .child(uid!!)
                .push()
            val addressPath = addressRef.toString().substring(addressRef.root.toString().length)

            sbAddress.key = addressRef.key

            val updates: MutableMap<String, Any?> = hashMapOf(
                addressPath to sbAddress
            )

            firebaseDatabase.reference.updateChildren(updates).await()
            Result.Success(Any())
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }

    override suspend fun deleteAddress(key: String): Result<Any> {
        return try {
            Result.Loading
            val user = firebaseAuth.currentUser
            val uid = user?.uid
            val addressRef = firebaseDatabase.reference.child(RealtimeDatabaseConstants.ADDRESS)
                .child(uid!!)
                .child(key)

            val addressPath = addressRef.toString().substring(addressRef.root.toString().length)

            val updates: MutableMap<String, Any?> = hashMapOf(
                addressPath to null,
            )

            firebaseDatabase.reference.updateChildren(updates)
            Result.Success(Any())
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }

    override suspend fun updateAddress(data: SbAddress): Result<Any> {
        return try {
            Result.Loading
            val user = firebaseAuth.currentUser
            val uid = user?.uid
            val reference = firebaseDatabase.reference.child(RealtimeDatabaseConstants.ADDRESS)
                .child(uid!!)
                .child(data.key!!)
            reference.updateChildren(data.toMap()).await()
            Result.Success(Any())
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }

    override suspend fun addShoes(sbShoes: SbShoes): Result<Any> {
        return try {
            Result.Loading
            val user = firebaseAuth.currentUser
            val uid = user?.uid
            val shoesRef = firebaseDatabase.reference.child(RealtimeDatabaseConstants.SHOES)
                .child(uid!!)
                .push()
            val shoesPath = shoesRef.toString().substring(shoesRef.root.toString().length)

            sbShoes.key = shoesRef.key

            val updates: MutableMap<String, Any?> = hashMapOf(
                shoesPath to sbShoes
            )

            firebaseDatabase.reference.updateChildren(updates).await()
            Result.Success(Any())
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }

    override suspend fun deleteShoes(key: String): Result<Any> {
        return try {
            Result.Loading
            val user = firebaseAuth.currentUser
            val uid = user?.uid
            val shoesRef = firebaseDatabase.reference.child(RealtimeDatabaseConstants.SHOES)
                .child(uid!!)
                .child(key)

            val shoesPath = shoesRef.toString().substring(shoesRef.root.toString().length)

            val updates: MutableMap<String, Any?> = hashMapOf(
                shoesPath to null
            )

            firebaseDatabase.reference.updateChildren(updates).await()
            Result.Success(Any())
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }

    override suspend fun updateShoes(data: SbShoes): Result<Any> {
        return try {
            Result.Loading
            val user = firebaseAuth.currentUser
            val uid = user?.uid
            val reference = firebaseDatabase.reference.child(RealtimeDatabaseConstants.SHOES)
                .child(uid!!)
                .child(data.key!!)
            reference.updateChildren(data.toMap()).await()
            Result.Success(Any())
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }
}