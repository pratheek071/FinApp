package com.example.finapp.presentation.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finapp.data.model.DailyPaymentSummary
import com.example.finapp.data.model.Loan
import com.example.finapp.data.model.LoanStatus
import com.example.finapp.data.repository.LoanRepository
import com.example.finapp.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {
    
    private val _adminState = MutableLiveData<AdminState>()
    val adminState: LiveData<AdminState> = _adminState
    
    private val _pendingLoans = MutableLiveData<List<Loan>>()
    val pendingLoans: LiveData<List<Loan>> = _pendingLoans
    
    private val _activeLoans = MutableLiveData<List<Loan>>()
    val activeLoans: LiveData<List<Loan>> = _activeLoans
    
    private val _dailySummary = MutableLiveData<DailyPaymentSummary>()
    val dailySummary: LiveData<DailyPaymentSummary> = _dailySummary
    
    fun loadPendingLoans() {
        viewModelScope.launch {
            _adminState.value = AdminState.Loading
            val result = loanRepository.getPendingLoans()
            result.fold(
                onSuccess = { loans ->
                    _pendingLoans.value = loans
                    _adminState.value = AdminState.PendingLoansLoaded
                },
                onFailure = { error ->
                    _adminState.value = AdminState.Error(error.message ?: "Failed to load pending loans")
                }
            )
        }
    }
    
    fun loadActiveLoans() {
        viewModelScope.launch {
            _adminState.value = AdminState.Loading
            val result = loanRepository.getApprovedLoans()
            result.fold(
                onSuccess = { loans ->
                    _activeLoans.value = loans
                    _adminState.value = AdminState.ActiveLoansLoaded
                },
                onFailure = { error ->
                    _adminState.value = AdminState.Error(error.message ?: "Failed to load active loans")
                }
            )
        }
    }
    
    fun loadDailySummary() {
        viewModelScope.launch {
            val result = paymentRepository.getDailyPaymentSummary()
            result.fold(
                onSuccess = { summary ->
                    _dailySummary.value = summary
                },
                onFailure = { error ->
                    _adminState.value = AdminState.Error(error.message ?: "Failed to load daily summary")
                }
            )
        }
    }
    
    fun approveLoan(loanId: String) {
        viewModelScope.launch {
            _adminState.value = AdminState.Loading
            val result = loanRepository.updateLoanStatus(loanId, LoanStatus.APPROVED)
            result.fold(
                onSuccess = {
                    _adminState.value = AdminState.Success("Loan approved successfully")
                    // Refresh both pending and active lists
                    loadPendingLoans()
                    loadActiveLoans()
                },
                onFailure = { error ->
                    _adminState.value = AdminState.Error(error.message ?: "Failed to approve loan")
                }
            )
        }
    }
    
    fun rejectLoan(loanId: String) {
        viewModelScope.launch {
            _adminState.value = AdminState.Loading
            val result = loanRepository.updateLoanStatus(loanId, LoanStatus.REJECTED)
            result.fold(
                onSuccess = {
                    _adminState.value = AdminState.Success("Loan rejected")
                    loadPendingLoans()
                },
                onFailure = { error ->
                    _adminState.value = AdminState.Error(error.message ?: "Failed to reject loan")
                }
            )
        }
    }
}

sealed class AdminState {
    object Loading : AdminState()
    object PendingLoansLoaded : AdminState()
    object ActiveLoansLoaded : AdminState()
    data class Success(val message: String) : AdminState()
    data class Error(val message: String) : AdminState()
}

