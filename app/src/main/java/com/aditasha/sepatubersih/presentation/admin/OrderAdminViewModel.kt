package com.aditasha.sepatubersih.presentation.admin

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.data.repository.OrderAdminRepositoryImpl
import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbArticle
import com.aditasha.sepatubersih.domain.model.SbOrder
import com.aditasha.sepatubersih.presentation.webUrlPattern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderAdminViewModel @Inject constructor(private val orderAdminRepositoryImpl: OrderAdminRepositoryImpl) :
    ViewModel() {

    private val _orderResult = MutableSharedFlow<Result<SbOrder>>()
    val orderResult: SharedFlow<Result<SbOrder>> = _orderResult

    var filterOrder = RealtimeDatabaseConstants.NEED_VERIF

    private val _articleResult = MutableSharedFlow<Result<Any>>()
    val articleResult: SharedFlow<Result<Any>> = _articleResult

    private val _articleForm = MutableSharedFlow<ArticleFormState>()
    val articleForm: SharedFlow<ArticleFormState> = _articleForm

    private val articleFormState = ArticleFormState()

    fun updateOrderStatus(sbOrder: SbOrder, node: String, status: String) {
        viewModelScope.launch {
            _orderResult.emit(orderAdminRepositoryImpl.updateOrderStatus(sbOrder, node, status))
        }
    }

    fun addArticle(sbArticle: SbArticle, imageUri: Uri) {
        viewModelScope.launch {
            _articleResult.emit(orderAdminRepositoryImpl.addArticle(sbArticle, imageUri))
        }
    }

    fun updateArticle(sbArticle: SbArticle, imageUri: Uri?) {
        viewModelScope.launch {
            _articleResult.emit(orderAdminRepositoryImpl.updateArticle(sbArticle, imageUri))
        }
    }

    fun deleteArticle(key: String) {
        viewModelScope.launch {
            orderAdminRepositoryImpl.deleteArticle(key)
        }
    }

    fun checkName(name: String?) {
        articleFormState.nameError = name?.isBlank()
        formValidator()
    }

    fun checkDesc(desc: String?) {
        articleFormState.descError = desc?.isBlank()
        formValidator()
    }

    fun checkLink(link: String?) {
        articleFormState.linkError =
            if (link == null) null else !webUrlPattern.matcher(link).matches() || link.isBlank()
        formValidator()
    }

    fun checkImage(imageError: Boolean?) {
        articleFormState.imageError = imageError
        formValidator()
    }

    private fun formValidator() {
        viewModelScope.launch {
            articleFormState.apply {
                if (nameError != true && descError != true && linkError != true && imageError != true) isDataValid =
                    true
            }
            _articleForm.emit(articleFormState)
        }
    }
}