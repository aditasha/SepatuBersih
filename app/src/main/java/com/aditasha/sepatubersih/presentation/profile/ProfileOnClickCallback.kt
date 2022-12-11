package com.aditasha.sepatubersih.presentation.profile

import android.os.Parcelable

interface ProfileOnClickCallback {
    fun onDeleteClicked(key: String)
    fun onEditClicked(data: Parcelable)
    fun onOrderClicked(data: Parcelable)
    fun onDataChanged()
}