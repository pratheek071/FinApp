package com.example.finapp.data.model

data class Loan(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val phoneNumber: String = "",
    val loanType: LoanType = LoanType.EDUCATION,
    val interestRate: Double = 0.0,
    val principalAmount: Double = 0.0,
    val durationMonths: Int = 0, // Now stores total months (years * 12)
    val totalAmount: Double = 0.0,
    val dailyAmount: Double = 0.0, // Now stores monthly amount
    val totalDays: Int = 0, // Now stores total months
    val status: LoanStatus = LoanStatus.PENDING,
    val requestedAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0
) {
    // Helper properties for clarity
    val monthlyAmount: Double get() = dailyAmount
    val totalMonths: Int get() = totalDays
    val durationYears: Int get() = durationMonths / 12
}

enum class LoanType(val displayName: String, val interestRate: Double) {
    EDUCATION("Education Loan", 6.88),
    PERSONAL("Personal Loan", 12.0),
    HOME("Home Loan", 7.0),
    CAR("Car Loan", 7.99);
    
    companion object {
        fun fromDisplayName(displayName: String): LoanType? {
            return values().find { it.displayName == displayName }
        }
    }
}

enum class LoanStatus {
    PENDING,    // Loan request submitted, awaiting admin approval
    APPROVED,   // Admin approved, loan is active
    REJECTED,   // Admin rejected the loan request
    COMPLETED   // Loan fully paid
}

