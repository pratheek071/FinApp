package com.example.finapp.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    
    private const val DATE_FORMAT = "dd MMM yyyy"
    private const val DATE_TIME_FORMAT = "dd MMM yyyy, hh:mm a"
    private const val TIME_FORMAT = "hh:mm a"
    
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun getEndOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
    
    fun addMonths(timestamp: Long, months: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.add(Calendar.MONTH, months)
        return calendar.timeInMillis
    }
    
    fun daysBetween(startTimestamp: Long, endTimestamp: Long): Int {
        val diff = endTimestamp - startTimestamp
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }
}

