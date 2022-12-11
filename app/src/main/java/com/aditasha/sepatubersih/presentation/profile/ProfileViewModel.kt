package com.aditasha.sepatubersih.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditasha.sepatubersih.data.repository.ProfileRepositoryImpl
import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbAddress
import com.aditasha.sepatubersih.domain.model.SbShoes
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val profileRepositoryImpl: ProfileRepositoryImpl) :
    ViewModel() {

    private val _addressResult = MutableSharedFlow<Result<Any>>()
    val addressResult: SharedFlow<Result<Any>> = _addressResult

    private val _addressName = MutableSharedFlow<Boolean>()
    val addressName: SharedFlow<Boolean> = _addressName

    private val _shoesResult = MutableSharedFlow<Result<Any>>()
    val shoesResult: SharedFlow<Result<Any>> = _shoesResult

    private val _shoesForm = MutableSharedFlow<ShoesFormState>()
    val shoesForm: SharedFlow<ShoesFormState> = _shoesForm

    val currentUser: FirebaseUser?
        get() = profileRepositoryImpl.currentUser

    fun addAddress(sbAddress: SbAddress) {
        viewModelScope.launch {
            _addressResult.emit(profileRepositoryImpl.addAddress(sbAddress))
        }
    }

    fun deleteAddress(key: String) {
        viewModelScope.launch {
            profileRepositoryImpl.deleteAddress(key)
        }
    }

    fun updateAddress(data: SbAddress) {
        viewModelScope.launch {
            _addressResult.emit(profileRepositoryImpl.updateAddress(data))
        }
    }

    fun checkAddressForm(string: String) {
        viewModelScope.launch {
            _addressName.emit(string.isNotBlank())
        }
    }

    fun addShoes(sbShoes: SbShoes) {
        viewModelScope.launch {
            _shoesResult.emit(profileRepositoryImpl.addShoes(sbShoes))
        }
    }

    fun deleteShoes(key: String) {
        viewModelScope.launch {
            profileRepositoryImpl.deleteShoes(key)
        }
    }

    fun updateShoes(data: SbShoes) {
        viewModelScope.launch {
            _shoesResult.emit(profileRepositoryImpl.updateShoes(data))
        }
    }

    fun checkShoesForm(name: String, brandType: String, color: String) {
        viewModelScope.launch {
            val shoesFormState =
                ShoesFormState(name.isBlank(), brandType.isBlank(), color.isBlank())
            shoesFormState.apply {
                if (!nameError && !brandTypeError && !colorError) isDataValid = true
            }
            _shoesForm.emit(shoesFormState)
        }
    }
}