package com.aditasha.sepatubersih.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SbAddress(
    var name: String? = null,
    var address: String? = null,
    var note: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var key: String? = null
) : Parcelable {

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "address" to address,
            "note" to note,
            "latitude" to latitude,
            "longitude" to longitude
        )
    }
}