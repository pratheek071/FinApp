package com.example.finapp.utils

import kotlin.math.roundToInt

object LoanCalculator {
    
    fun calculateLoanDetails(
        principalAmount: Double,
        interestRate: Double,
        durationYears: Int
    ): LoanCalculation {
        // Calculate total months
        val totalMonths = durationYears * 12
        
        // Calculate total interest based on simple interest (annual rate * years)
        val totalInterest = (principalAmount * interestRate * durationYears) / 100
        val totalAmount = principalAmount + totalInterest
        
        // Calculate monthly payment amount
        val monthlyAmount = totalAmount / totalMonths
        
        return LoanCalculation(
            principalAmount = principalAmount,
            interestRate = interestRate,
            durationYears = durationYears,
            durationMonths = totalMonths,
            totalInterest = totalInterest.roundTo2Decimals(),
            totalAmount = totalAmount.roundTo2Decimals(),
            monthlyAmount = monthlyAmount.roundTo2Decimals(),
            totalMonths = totalMonths
        )
    }
    
    private fun Double.roundTo2Decimals(): Double {
        return (this * 100.0).roundToInt() / 100.0
    }
}

data class LoanCalculation(
    val principalAmount: Double,
    val interestRate: Double,
    val durationYears: Int,
    val durationMonths: Int, // Total months for storage
    val totalInterest: Double,
    val totalAmount: Double,
    val monthlyAmount: Double,
    val totalMonths: Int
) {
    // Legacy properties for backward compatibility
    val dailyAmount: Double get() = monthlyAmount
    val durationDays: Int get() = totalMonths
}

