package com.example.finapp.data.repository

import android.app.Activity
import com.example.finapp.data.model.User
import com.example.finapp.data.model.UserRole
import com.example.finapp.utils.Constants
import com.example.finapp.utils.PreferenceManager
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val preferenceManager: PreferenceManager
) {
    
    var storedVerificationId: String? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    
    suspend fun sendOTP(phoneNumber: String, activity: Activity): Result<String> {
        return suspendCoroutine { continuation ->
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        // Auto-verification completed
                        continuation.resume(Result.success("Auto-verified"))
                    }
                    
                    override fun onVerificationFailed(e: FirebaseException) {
                        continuation.resume(Result.failure(e))
                    }
                    
                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        storedVerificationId = verificationId
                        resendToken = token
                        continuation.resume(Result.success(verificationId))
                    }
                })
                .build()
            
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }
    
    suspend fun verifyOTP(verificationId: String, code: String): Result<String> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID is null")
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createOrGetUser(phoneNumber: String, name: String, role: UserRole): Result<User> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: throw Exception("Not authenticated")
            
            // Check if user already exists
            val userDoc = firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
            
            val user = if (userDoc.exists()) {
                userDoc.toObject(User::class.java) ?: throw Exception("Failed to parse user")
            } else {
                // Create new user
                val newUser = User(
                    id = userId,
                    phoneNumber = phoneNumber,
                    name = name,
                    role = role,
                    createdAt = System.currentTimeMillis()
                )
                firestore.collection(Constants.COLLECTION_USERS)
                    .document(userId)
                    .set(newUser)
                    .await()
                newUser
            }
            
            // Save to preferences
            preferenceManager.userId = user.id
            preferenceManager.userRole = user.role
            preferenceManager.userName = user.name
            preferenceManager.userPhone = user.phoneNumber
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUser(userId: String): Result<User> {
        return try {
            val userDoc = firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
            
            val user = userDoc.toObject(User::class.java) 
                ?: throw Exception("User not found")
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun signOut() {
        firebaseAuth.signOut()
        preferenceManager.clear()
    }
    
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}

