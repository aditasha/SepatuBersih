package com.aditasha.sepatubersih.domain.repository

import android.net.Uri
import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbArticle
import com.aditasha.sepatubersih.domain.model.SbOrder
import com.google.firebase.auth.FirebaseUser

interface OrderAdminRepository {
    val currentUser: FirebaseUser?
    suspend fun updateOrderStatus(sbOrder: SbOrder, node: String, status: String): Result<SbOrder>
    suspend fun addArticle(sbArticle: SbArticle, imageUri: Uri): Result<String>
    suspend fun updateArticle(sbArticle: SbArticle, imageUri: Uri?): Result<String>
    suspend fun deleteArticle(key: String): Result<Any>
}