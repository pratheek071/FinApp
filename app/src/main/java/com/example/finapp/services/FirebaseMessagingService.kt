package com.example.finapp.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.finapp.MainActivity
import com.example.finapp.R
import com.example.finapp.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {
    
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        message.notification?.let {
            showNotification(it.title ?: "FinApp", it.body ?: "")
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Send token to server if needed
    }
    
    private fun showNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(System.currentTimeMillis().toInt(), notification)
    }
}

