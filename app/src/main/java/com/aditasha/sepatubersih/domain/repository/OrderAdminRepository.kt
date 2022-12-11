package com.aditasha.sepatubersih.domain.repository

import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbOrder
import com.google.firebase.auth.FirebaseUser

interface OrderAdminRepository {
    val currentUser: FirebaseUser?
    suspend fun updateOrderStatus(sbOrder: SbOrder, node: String, status: String): Result<SbOrder>
}