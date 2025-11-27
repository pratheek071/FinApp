package com.example.finapp.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FCMHelper @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    /**
     * Initialize FCM token for the current user
     * Call this after user logs in
     */
    suspend fun initializeFCM(userId: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d("FCMHelper", "FCM Token obtained: $token")
            
            // Save token to Firestore
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update("fcmToken", token)
                .await()
                
            Log.d("FCMHelper", "FCM Token saved to Firestore for user: $userId")
        } catch (e: Exception) {
            Log.e("FCMHelper", "Error initializing FCM token", e)
        }
    }
    
    /**
     * Update FCM token for a user
     * Call this when token is refreshed
     */
    suspend fun updateFCMToken(userId: String, token: String) {
        try {
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update("fcmToken", token)
                .await()
                
            Log.d("FCMHelper", "FCM Token updated for user: $userId")
        } catch (e: Exception) {
            Log.e("FCMHelper", "Error updating FCM token", e)
        }
    }
    
    /**
     * Get FCM token for a specific user
     */
    suspend fun getUserToken(userId: String): String? {
        return try {
            val doc = firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
            doc.getString("fcmToken")
        } catch (e: Exception) {
            Log.e("FCMHelper", "Error getting user token", e)
            null
        }
    }
    
    /**
     * Get all admin FCM tokens
     */
    suspend fun getAdminTokens(): List<String> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_USERS)
                .whereEqualTo("role", "ADMIN")
                .get()
                .await()
            
            snapshot.documents.mapNotNull { it.getString("fcmToken") }
                .filter { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.e("FCMHelper", "Error getting admin tokens", e)
            emptyList()
        }
    }
}

