package com.aditasha.sepatubersih.data.repository

import android.net.Uri
import com.aditasha.sepatubersih.ServerTime
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbOrder
import com.aditasha.sepatubersih.domain.model.SbOrderItem
import com.aditasha.sepatubersih.domain.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.*
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val serverTime: ServerTime
) : OrderRepository {

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun addOrder(sbOrder: SbOrder): Result<String> {
        return try {
            Result.Loading
            val timeStamp = Date().time + serverTime.getServerTime()
            val user = currentUser!!

            val orderItemRef =
                firebaseDatabase.reference.child(RealtimeDatabaseConstants.USER_ORDER_ITEM)
                    .child(RealtimeDatabaseConstants.ONGOING)
                    .child(user.uid)
                    .push()

            val orderItemPath =
                orderItemRef.toString().substring(orderItemRef.root.toString().length)
            val key = orderItemRef.key

            val orderDetailRef =
                firebaseDatabase.reference.child(RealtimeDatabaseConstants.ORDER_DETAIL)
                    .child(key!!)

            val orderDetailPath =
                orderDetailRef.toString().substring(orderDetailRef.root.toString().length)

            val orderNodeRef =
                firebaseDatabase.reference.child(RealtimeDatabaseConstants.ORDER_NODE)
                    .child(key)

            val orderNodePath =
                orderNodeRef.toString().substring(orderNodeRef.root.toString().length)

            val id = "SB-" + key.substring(key.lastIndex - 5, key.length)
            sbOrder.key = key
            sbOrder.id = id
            sbOrder.uid = user.uid
            sbOrder.orderTimestamp = timeStamp
            sbOrder.name = user.displayName
            sbOrder.email = user.email
            sbOrder.number = user.phoneNumber
            sbOrder.status = RealtimeDatabaseConstants.NEED_PAYMENT
            sbOrder.statusTimestamp = timeStamp
            sbOrder.endTimestamp = timeStamp + 600000

            var sbOrderItem: SbOrderItem
            sbOrder.apply {
                sbOrderItem = SbOrderItem(
                    this.key, id, name, orderTimestamp, status, statusTimestamp, endTimestamp, price
                )
            }

            val updates: MutableMap<String, Any?> = hashMapOf(
                orderItemPath to sbOrderItem,
                orderDetailPath to sbOrder,
                orderNodePath to RealtimeDatabaseConstants.ONGOING
            )

            firebaseDatabase.reference.updateChildren(updates).await()

            Result.Success(key)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }

    override suspend fun updateOrderStatus(
        sbOrder: SbOrder,
        node: String,
        status: String,
        proof: String
    ): Result<SbOrder> {
        return try {
            Result.Loading
            val user = currentUser!!
            val ongoingItemRef =
                firebaseDatabase.reference.child(RealtimeDatabaseConstants.USER_ORDER_ITEM)
                    .child(RealtimeDatabaseConstants.ONGOING)
                    .child(user.uid)
                    .child(sbOrder.key!!)

            val completeItemRef =
                firebaseDatabase.reference.child(RealtimeDatabaseConstants.USER_ORDER_ITEM)
                    .child(RealtimeDatabaseConstants.COMPLETE)
                    .child(user.uid)
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

            // Complete, cancelled or expired
            if (node == RealtimeDatabaseConstants.COMPLETE) {
                when (status) {
                    RealtimeDatabaseConstants.STATUS_COMPLETE -> {
                        sbOrder.statusTimestamp = timeStamp
                        sbOrder.status = RealtimeDatabaseConstants.STATUS_COMPLETE
                    }
                    RealtimeDatabaseConstants.EXPIRED -> {
                        sbOrder.statusTimestamp = sbOrder.endTimestamp
                        sbOrder.status = RealtimeDatabaseConstants.EXPIRED
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
//                    allOrderPath to sbOrderItem,
//                    orderDetailPath to sbOrder,
                    orderNodePath to RealtimeDatabaseConstants.COMPLETE
                )

                val statusUpdate: MutableMap<String, Any?> = hashMapOf(
                    "statusTimestamp" to timeStamp,
                    "status" to status,
                )

                if (status != RealtimeDatabaseConstants.EXPIRED) {
                    allOrderRef.updateChildren(statusUpdate).await()
                }

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

                if (status == RealtimeDatabaseConstants.NEED_VERIF) {
                    if (proof.isNotBlank()) {
                        updates["proof"] = proof
                        orderDetailRef.updateChildren(updates).await()
                    }

                    sbOrder.statusTimestamp = timeStamp
                    sbOrder.status = status

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
                    allOrderRef.setValue(sbOrderItem).await()
                } else if (status == RealtimeDatabaseConstants.CUSTOMER_PICKUP || status == RealtimeDatabaseConstants.QUEUE_FOR_DELIV) {
                    allOrderRef.updateChildren(updates).await()
                    orderDetailRef.updateChildren(updates).await()
                } else {
                    orderDetailRef.updateChildren(updates).await()
                }
            }

            Result.Success(sbOrder)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }

    //Expired timer from list
    override suspend fun updateOrderFromItem(sbOrderItem: SbOrderItem): Result<Any> {
        return try {
            Result.Loading
            val user = currentUser!!
            val ongoingItemRef =
                firebaseDatabase.reference.child(RealtimeDatabaseConstants.USER_ORDER_ITEM)
                    .child(RealtimeDatabaseConstants.ONGOING)
                    .child(user.uid)
                    .child(sbOrderItem.key!!)

            val completeItemRef =
                firebaseDatabase.reference.child(RealtimeDatabaseConstants.USER_ORDER_ITEM)
                    .child(RealtimeDatabaseConstants.COMPLETE)
                    .child(user.uid)
                    .child(sbOrderItem.key!!)

            val orderDetailRef =
                firebaseDatabase.reference.child(RealtimeDatabaseConstants.ORDER_DETAIL)
                    .child(sbOrderItem.key!!)

            val orderNodeRef =
                firebaseDatabase.reference.child(RealtimeDatabaseConstants.ORDER_NODE)
                    .child(sbOrderItem.key!!)

            val ongoingItemPath =
                ongoingItemRef.toString().substring(ongoingItemRef.root.toString().length)
            val completeItemPath =
                completeItemRef.toString().substring(completeItemRef.root.toString().length)
            val orderNodePath =
                orderNodeRef.toString().substring(orderNodeRef.root.toString().length)

            sbOrderItem.statusTimestamp = sbOrderItem.endTimestamp
            sbOrderItem.status = RealtimeDatabaseConstants.EXPIRED

            val updateDetail: MutableMap<String, Any?> = hashMapOf(
                "statusTimestamp" to sbOrderItem.statusTimestamp,
                "status" to sbOrderItem.status,
            )

            val updates: MutableMap<String, Any?> = hashMapOf(
                ongoingItemPath to null,
                completeItemPath to sbOrderItem,
                orderNodePath to RealtimeDatabaseConstants.COMPLETE
            )

            orderDetailRef.updateChildren(updateDetail).await()
            firebaseDatabase.reference.updateChildren(updates).await()
            Result.Success(Any())
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }

    override suspend fun uploadProof(file: File, key: String): Result<String> {
        return try {
            Result.Loading
            val storage = firebaseStorage.reference
                .child(RealtimeDatabaseConstants.ORDER_DETAIL)
                .child(key)
                .child(file.name)
            storage.putFile(Uri.fromFile(file)).await()
            Result.Success(file.name)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }

}