package com.aditasha.sepatubersih.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.data.repository.OrderAdminRepositoryImpl
import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbOrder
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

    private val _uploadProof = MutableSharedFlow<Result<String>>()
    val uploadProof: SharedFlow<Result<String>> = _uploadProof

    var filterOrder = RealtimeDatabaseConstants.NEED_VERIF

    fun updateOrderStatus(sbOrder: SbOrder, node: String, status: String) {
        viewModelScope.launch {
            _orderResult.emit(orderAdminRepositoryImpl.updateOrderStatus(sbOrder, node, status))
        }
    }
}