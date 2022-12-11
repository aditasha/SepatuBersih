package com.aditasha.sepatubersih.presentation.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditasha.sepatubersih.data.repository.OrderRepositoryImpl
import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbOrder
import com.aditasha.sepatubersih.domain.model.SbOrderItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(private val orderRepositoryImpl: OrderRepositoryImpl) :
    ViewModel() {

    private val _orderResult = MutableSharedFlow<Result<String>>()
    val orderResult: SharedFlow<Result<String>> = _orderResult

    private val _updateResult = MutableSharedFlow<Result<Any>>()
    val updateResult: SharedFlow<Result<Any>> = _updateResult

    private val _uploadProof = MutableSharedFlow<Result<String>>()
    val uploadProof: SharedFlow<Result<String>> = _uploadProof

    val currentUser
        get() = orderRepositoryImpl.currentUser

    fun addOrder(sbOrder: SbOrder) {
        viewModelScope.launch {
            _orderResult.emit(orderRepositoryImpl.addOrder(sbOrder))
        }
    }

    fun updateOrderStatus(sbOrder: SbOrder, node: String, status: String, proof: String = "") {
        viewModelScope.launch {
            if (proof.isBlank()) orderRepositoryImpl.updateOrderStatus(sbOrder, node, status)
            else orderRepositoryImpl.updateOrderStatus(sbOrder, node, status, proof)
        }
    }

    fun updateOrderFromItem(sbOrderItem: SbOrderItem) {
        viewModelScope.launch {
            _updateResult.emit(orderRepositoryImpl.updateOrderFromItem(sbOrderItem))
        }
    }

    fun uploadProof(file: File, key: String) {
        viewModelScope.launch {
            _uploadProof.emit(orderRepositoryImpl.uploadProof(file, key))
        }
    }
}