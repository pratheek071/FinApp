package com.example.finapp.presentation.client

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finapp.data.model.Payment
import com.example.finapp.data.model.PaymentStatus
import com.example.finapp.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {
    
    private val _paymentState = MutableLiveData<PaymentState>()
    val paymentState: LiveData<PaymentState> = _paymentState
    
    private val _todayPayment = MutableLiveData<Payment?>()
    val todayPayment: LiveData<Payment?> = _todayPayment
    
    fun submitPayment(payment: Payment) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            val result = paymentRepository.createPayment(payment)
            result.fold(
                onSuccess = {
                    _paymentState.value = PaymentState.Success("Payment recorded successfully")
                },
                onFailure = { error ->
                    _paymentState.value = PaymentState.Error(error.message ?: "Failed to record payment")
                }
            )
        }
    }
    
    fun checkTodayPayment(loanId: String) {
        viewModelScope.launch {
            val result = paymentRepository.getTodayPaymentForLoan(loanId)
            result.fold(
                onSuccess = { payment ->
                    _todayPayment.value = payment
                },
                onFailure = {
                    _todayPayment.value = null
                }
            )
        }
    }
    
    fun getLoanPayments(loanId: String) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            val result = paymentRepository.getLoanPayments(loanId)
            result.fold(
                onSuccess = { payments ->
                    _paymentState.value = PaymentState.PaymentsLoaded(payments)
                },
                onFailure = { error ->
                    _paymentState.value = PaymentState.Error(error.message ?: "Failed to load payments")
                }
            )
        }
    }
}

sealed class PaymentState {
    object Loading : PaymentState()
    data class Success(val message: String) : PaymentState()
    data class PaymentsLoaded(val payments: List<Payment>) : PaymentState()
    data class Error(val message: String) : PaymentState()
}

