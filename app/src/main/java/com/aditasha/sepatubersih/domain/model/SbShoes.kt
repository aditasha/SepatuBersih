package com.aditasha.sepatubersih.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SbShoes(
    var name: String? = null,
    var brandType: String? = null,
    var color: String? = null,
    var notes: String? = null,
    var key: String? = null
) : Parcelable {

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "brandType" to brandType,
            "color" to color,
            "notes" to notes,
            "key" to key,
        )
    }
}
