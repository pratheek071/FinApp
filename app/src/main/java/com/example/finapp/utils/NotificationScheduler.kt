package com.example.finapp.utils

import androidx.work.*
import com.example.finapp.workers.DailyReminderWorker
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    
    fun scheduleDailyReminders() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            Constants.NOTIFICATION_WORKER_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }
    
    fun cancelDailyReminders() {
        workManager.cancelUniqueWork(Constants.NOTIFICATION_WORKER_NAME)
    }
    
    private fun calculateInitialDelay(): Long {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.timeInMillis
        
        // Set target time to 9:00 AM
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // If 9:00 AM has passed today, schedule for tomorrow
        if (calendar.timeInMillis < currentTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return calendar.timeInMillis - currentTime
    }
}

