package com.aditasha.sepatubersih.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SbOrder(
    var key: String? = null,
    var id: String? = null,
    var uid: String? = null,
    var orderTimestamp: Long? = null,
    var name: String? = null,
    var email: String? = null,
    var number: String? = null,
    var address: SbAddress? = null,
    var shoes: ArrayList<SbShoes>? = null,
    var status: String? = null,
    var statusTimestamp: Long? = null,
    var endTimestamp: Long? = null,
    var price: Double? = null,
    var proof: String? = null,
) : Parcelable {

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "key" to key,
            "id" to id,
            "orderTimestamp" to orderTimestamp,
            "address" to address,
            "name" to name,
            "shoes" to shoes,
            "status" to status,
            "statusTimestamp" to statusTimestamp,
            "price" to price
        )
    }
}