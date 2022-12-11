package com.aditasha.sepatubersih.domain.repository

import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbAddress
import com.aditasha.sepatubersih.domain.model.SbShoes
import com.google.firebase.auth.FirebaseUser

interface ProfileRepository {
    val currentUser: FirebaseUser?
    suspend fun addAddress(sbAddress: SbAddress): Result<Any>
    suspend fun deleteAddress(key: String): Result<Any>
    suspend fun updateAddress(data: SbAddress): Result<Any>
    suspend fun addShoes(sbShoes: SbShoes): Result<Any>
    suspend fun deleteShoes(key: String): Result<Any>
    suspend fun updateShoes(data: SbShoes): Result<Any>
}