package com.aditasha.sepatubersih.presentation.admin

import android.os.Parcelable

interface ArticleAdminOnClickCallback {
    fun onArticleClicked(link: String)
    fun onArticleEdit(data: Parcelable)
    fun onArticleDelete(key: String, name: String)
    fun onDataChanged()
}