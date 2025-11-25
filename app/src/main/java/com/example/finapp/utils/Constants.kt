package com.example.finapp.utils

object Constants {
    // Firestore Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_LOANS = "loans"
    const val COLLECTION_PAYMENTS = "payments"
    
    // SharedPreferences
    const val PREF_NAME = "FinAppPrefs"
    const val PREF_USER_ID = "userId"
    const val PREF_USER_ROLE = "userRole"
    const val PREF_USER_NAME = "userName"
    const val PREF_USER_PHONE = "userPhone"
    
    // Notification
    const val NOTIFICATION_CHANNEL_ID = "loan_payment_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Loan Payment Reminders"
    const val NOTIFICATION_WORKER_NAME = "daily_payment_reminder"
    
    // Admin UPI ID (for receiving payments)
    const val ADMIN_UPI_ID = "6364475759@ybl"  // Replace with actual UPI ID
    const val PAYMENT_RECEIVER_NAME = "FinApp"
}

