package com.example.finapp.utils

import kotlin.math.roundToInt

object LoanCalculator {
    
    fun calculateLoanDetails(
        principalAmount: Double,
        interestRate: Double,
        durationMonths: Int
    ): LoanCalculation {
        // Calculate total interest based on simple interest
        val totalInterest = (principalAmount * interestRate * durationMonths) / (100 * 12)
        val totalAmount = principalAmount + totalInterest
        
        // Calculate duration in days (approximation: 30 days per month)
        val durationDays = durationMonths * 30
        val dailyAmount = totalAmount / durationDays
        
        return LoanCalculation(
            principalAmount = principalAmount,
            interestRate = interestRate,
            durationMonths = durationMonths,
            totalInterest = totalInterest.roundTo2Decimals(),
            totalAmount = totalAmount.roundTo2Decimals(),
            dailyAmount = dailyAmount.roundTo2Decimals(),
            durationDays = durationDays
        )
    }
    
    private fun Double.roundTo2Decimals(): Double {
        return (this * 100.0).roundToInt() / 100.0
    }
}

data class LoanCalculation(
    val principalAmount: Double,
    val interestRate: Double,
    val durationMonths: Int,
    val totalInterest: Double,
    val totalAmount: Double,
    val dailyAmount: Double,
    val durationDays: Int
)

