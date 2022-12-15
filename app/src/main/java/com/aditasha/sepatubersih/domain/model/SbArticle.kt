package com.aditasha.sepatubersih.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SbArticle(
    var key: String? = null,
    var timestamp: Long? = null,
    var reverseStamp: Long? = null,
    var image: String? = null,
    var name: String? = null,
    var desc: String? = null,
    var link: String? = null
) : Parcelable {

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "key" to key,
            "timestamp" to timestamp,
            "reverseStamp" to reverseStamp,
            "image" to image,
            "name" to name,
            "desc" to desc,
            "link" to link
        )
    }

}