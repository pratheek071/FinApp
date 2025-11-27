package com.example.finapp.presentation.auth

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finapp.data.model.User
import com.example.finapp.data.model.UserRole
import com.example.finapp.data.repository.AuthRepository
import com.example.finapp.utils.FCMHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val fcmHelper: FCMHelper
) : ViewModel() {
    
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user
    
    fun sendOTP(phoneNumber: String, activity: Activity) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.sendOTP(phoneNumber, activity)
            result.fold(
                onSuccess = { verificationId ->
                    _authState.value = AuthState.OTPSent(verificationId)
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Failed to send OTP")
                }
            )
        }
    }
    
    fun verifyOTP(verificationId: String, code: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.verifyOTP(verificationId, code)
            result.fold(
                onSuccess = {
                    _authState.value = AuthState.OTPVerified
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Failed to verify OTP")
                }
            )
        }
    }
    
    fun createOrGetUser(phoneNumber: String, name: String, role: UserRole) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.createOrGetUser(phoneNumber, name, role)
            result.fold(
                onSuccess = { user ->
                    _user.value = user
                    
                    // Initialize FCM token for this user
                    fcmHelper.initializeFCM(user.id)
                    
                    _authState.value = AuthState.Authenticated(user)
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Failed to create user")
                }
            )
        }
    }
    
    fun signOut() {
        authRepository.signOut()
    }
}

sealed class AuthState {
    object Loading : AuthState()
    data class OTPSent(val verificationId: String) : AuthState()
    object OTPVerified : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

