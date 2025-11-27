package com.example.finapp.workers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.finapp.MainActivity
import com.example.finapp.R
import com.example.finapp.data.repository.LoanRepository
import com.example.finapp.data.repository.PaymentRepository
import com.example.finapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val loanRepository = LoanRepository(FirebaseFirestore.getInstance())
    private val paymentRepository = PaymentRepository(
        FirebaseFirestore.getInstance(),
        loanRepository
    )
    
    override suspend fun doWork(): Result {
        return try {
            // Get all approved (active) loans
            val activeLoans = loanRepository.getApprovedLoans().getOrNull() ?: emptyList()
            
            activeLoans.forEach { loan ->
                // Check if payment is made this month for this loan
                val todayPayment = paymentRepository.getTodayPaymentForLoan(loan.id).getOrNull()
                
                if (todayPayment == null) {
                    // Send reminder notification
                    sendReminderNotification(
                        userId = loan.userId,
                        userName = loan.userName,
                        amount = loan.monthlyAmount
                    )
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
    
    private fun sendReminderNotification(userId: String, userName: String, amount: Double) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Monthly Payment Reminder")
            .setContentText("Hi $userName, your monthly payment of ₹$amount is due this month!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(userId.hashCode(), notification)
    }
}

