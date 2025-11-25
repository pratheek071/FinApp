package com.example.finapp.presentation.client

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finapp.data.model.Loan
import com.example.finapp.data.repository.LoanRepository
import com.example.finapp.utils.LoanCalculation
import com.example.finapp.utils.LoanCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoanViewModel @Inject constructor(
    private val loanRepository: LoanRepository
) : ViewModel() {
    
    private val _loanState = MutableLiveData<LoanState>()
    val loanState: LiveData<LoanState> = _loanState
    
    private val _userLoans = MutableLiveData<List<Loan>>()
    val userLoans: LiveData<List<Loan>> = _userLoans
    
    private val _loanCalculation = MutableLiveData<LoanCalculation>()
    val loanCalculation: LiveData<LoanCalculation> = _loanCalculation
    
    fun calculateLoan(principalAmount: Double, interestRate: Double, durationMonths: Int) {
        val calculation = LoanCalculator.calculateLoanDetails(
            principalAmount,
            interestRate,
            durationMonths
        )
        _loanCalculation.value = calculation
    }
    
    fun submitLoan(loan: Loan) {
        viewModelScope.launch {
            _loanState.value = LoanState.Loading
            val result = loanRepository.createLoan(loan)
            result.fold(
                onSuccess = {
                    _loanState.value = LoanState.Success("Loan request submitted successfully")
                },
                onFailure = { error ->
                    _loanState.value = LoanState.Error(error.message ?: "Failed to submit loan")
                }
            )
        }
    }
    
    fun loadUserLoans(userId: String) {
        viewModelScope.launch {
            _loanState.value = LoanState.Loading
            val result = loanRepository.getUserLoans(userId)
            result.fold(
                onSuccess = { loans ->
                    _userLoans.value = loans
                    _loanState.value = LoanState.LoansLoaded
                },
                onFailure = { error ->
                    _loanState.value = LoanState.Error(error.message ?: "Failed to load loans")
                }
            )
        }
    }
    
    fun getLoan(loanId: String) {
        viewModelScope.launch {
            val result = loanRepository.getLoan(loanId)
            result.fold(
                onSuccess = { loan ->
                    _loanState.value = LoanState.LoanLoaded(loan)
                },
                onFailure = { error ->
                    _loanState.value = LoanState.Error(error.message ?: "Failed to load loan")
                }
            )
        }
    }
}

sealed class LoanState {
    object Loading : LoanState()
    object LoansLoaded : LoanState()
    data class LoanLoaded(val loan: Loan) : LoanState()
    data class Success(val message: String) : LoanState()
    data class Error(val message: String) : LoanState()
}

