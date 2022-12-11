package com.aditasha.sepatubersih.domain.repository

import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbOrder
import com.aditasha.sepatubersih.domain.model.SbOrderItem
import com.google.firebase.auth.FirebaseUser
import java.io.File

interface OrderRepository {
    val currentUser: FirebaseUser?
    suspend fun addOrder(sbOrder: SbOrder): Result<String>
    suspend fun updateOrderStatus(
        sbOrder: SbOrder,
        node: String,
        status: String,
        proof: String = ""
    ): Result<SbOrder>

    suspend fun updateOrderFromItem(sbOrderItem: SbOrderItem): Result<Any>
    suspend fun uploadProof(file: File, key: String): Result<String>
}