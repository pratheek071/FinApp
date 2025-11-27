package com.example.finapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.finapp.MainActivity
import com.example.finapp.R
import com.example.finapp.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    private val firestore = FirebaseFirestore.getInstance()
    
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d("FCM", "Message received from: ${message.from}")
        
        // Handle notification payload
        message.notification?.let {
            val title = it.title ?: "FinApp"
            val body = it.body ?: ""
            showNotification(title, body, message.data)
        }
        
        // Handle data payload
        if (message.data.isNotEmpty()) {
            Log.d("FCM", "Message data: ${message.data}")
            handleDataPayload(message.data)
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        Log.d("FCM", "New FCM token: $token")
        
        // Update token in Firestore for current user
        val prefs = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        val userId = prefs.getString(Constants.PREF_USER_ID, null)
        
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    firestore.collection(Constants.COLLECTION_USERS)
                        .document(userId)
                        .update("fcmToken", token)
                    Log.d("FCM", "Token updated in Firestore for user: $userId")
                } catch (e: Exception) {
                    Log.e("FCM", "Error updating token in Firestore", e)
                }
            }
        }
    }
    
    private fun showNotification(title: String, message: String, data: Map<String, String>) {
        createNotificationChannel()
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add data for navigation
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Loan notifications and updates"
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun handleDataPayload(data: Map<String, String>) {
        val type = data["type"]
        val loanId = data["loanId"]
        
        Log.d("FCM", "Handling data payload - Type: $type, LoanId: $loanId")
        
        when (type) {
            "NEW_LOAN" -> {
                // Handle new loan notification
                Log.d("FCM", "New loan request received")
            }
            "LOAN_STATUS" -> {
                // Handle loan status update
                val status = data["status"]
                Log.d("FCM", "Loan status update: $status")
            }
        }
    }
}

