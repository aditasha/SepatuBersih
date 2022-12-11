package com.aditasha.sepatubersih.data.repository

import com.aditasha.sepatubersih.ServerTime
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbOrder
import com.aditasha.sepatubersih.domain.model.SbOrderItem
import com.aditasha.sepatubersih.domain.repository.OrderAdminRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class OrderAdminRepositoryImpl @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val serverTime: ServerTime
) : OrderAdminRepository {

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun updateOrderStatus(
        sbOrder: SbOrder,
        node: String,
        status: String
    ): Result<SbOrder> {
        return try {
            Result.Loading
            val ongoingItemRef =
                firebaseDatabase.reference.child(RealtimeDatabaseConstants.USER_ORDER_ITEM)
                    .child(RealtimeDatabaseConstants.ONGOING)
                    .child(sbOrder.uid!!)
                    .child(sbOrder.key!!)

            val completeItemRef =
                firebaseDatabase.reference.child(RealtimeDatabaseConstants.USER_ORDER_ITEM)
                    .child(RealtimeDatabaseConstants.COMPLETE)
                    .child(sbOrder.uid!!)
                    .child(sbOrder.key!!)

            val orderDetailRef =
                firebaseDatabase.reference.child(RealtimeDatabaseConstants.ORDER_DETAIL)
                    .child(sbOrder.key!!)

            val allOrderRef =
                firebaseDatabase.reference.child(RealtimeDatabaseConstants.ALL_ORDER_ITEM)
                    .child(sbOrder.key!!)

            val orderNodeRef =
                firebaseDatabase.reference.child(RealtimeDatabaseConstants.ORDER_NODE)
                    .child(sbOrder.key!!)

            val ongoingItemPath =
                ongoingItemRef.toString().substring(ongoingItemRef.root.toString().length)
            val completeItemPath =
                completeItemRef.toString().substring(completeItemRef.root.toString().length)
            val orderNodePath =
                orderNodeRef.toString().substring(orderNodeRef.root.toString().length)

            var sbOrderItem: SbOrderItem
            val timeStamp = Date().time + serverTime.getServerTime()

            // Complete or cancelled
            if (node == RealtimeDatabaseConstants.COMPLETE) {
                when (status) {
                    RealtimeDatabaseConstants.STATUS_COMPLETE -> {
                        sbOrder.statusTimestamp = timeStamp
                        sbOrder.status = RealtimeDatabaseConstants.STATUS_COMPLETE
                    }
                    //Cancelled
                    else -> {
                        sbOrder.statusTimestamp = timeStamp
                        sbOrder.status = RealtimeDatabaseConstants.CANCELLED
                    }
                }

                sbOrder.apply {
                    sbOrderItem = SbOrderItem(
                        this.key,
                        id,
                        name,
                        orderTimestamp,
                        this.status,
                        statusTimestamp,
                        endTimestamp,
                        price
                    )
                }

                val updates: MutableMap<String, Any?> = hashMapOf(
                    ongoingItemPath to null,
                    completeItemPath to sbOrderItem,
                    orderNodePath to RealtimeDatabaseConstants.COMPLETE
                )

                val statusUpdate: MutableMap<String, Any?> = hashMapOf(
                    "statusTimestamp" to timeStamp,
                    "status" to status,
                )

                allOrderRef.updateChildren(statusUpdate).await()
                orderDetailRef.updateChildren(statusUpdate).await()
                firebaseDatabase.reference.updateChildren(updates).await()
            }

            //Ongoing
            else {
                val updates: MutableMap<String, Any?> = hashMapOf(
                    "statusTimestamp" to timeStamp,
                    "status" to status,
                )

                ongoingItemRef.updateChildren(updates).await()
                orderDetailRef.updateChildren(updates).await()
                allOrderRef.updateChildren(updates).await()
            }

            Result.Success(sbOrder)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }

}