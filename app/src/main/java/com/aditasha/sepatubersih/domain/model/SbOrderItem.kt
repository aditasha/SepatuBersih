package com.aditasha.sepatubersih.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SbOrderItem(
    var key: String? = null,
    var id: String? = null,
    var name: String? = null,
    var orderTimestamp: Long? = null,
    var status: String? = null,
    var statusTimestamp: Long? = null,
    var endTimestamp: Long? = null,
    var price: Double? = null,
    var driver: String? = null
) : Parcelable {

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "key" to key,
            "orderTimestamp" to orderTimestamp,
            "status" to status,
            "statusTimestamp" to statusTimestamp,
            "price" to price
        )
    }
}