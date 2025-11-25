package com.example.finapp.data.model

data class Loan(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val phoneNumber: String = "",
    val loanType: LoanType = LoanType.EDUCATION,
    val interestRate: Double = 0.0,
    val principalAmount: Double = 0.0,
    val durationMonths: Int = 0,
    val totalAmount: Double = 0.0,
    val dailyAmount: Double = 0.0,
    val totalDays: Int = 0,
    val status: LoanStatus = LoanStatus.PENDING,
    val requestedAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0
)

enum class LoanType(val displayName: String, val interestRate: Double) {
    EDUCATION("Education Loan", 5.0),
    PERSONAL("Personal Loan", 5.5),
    HOME("Home Loan", 6.0),
    CAR("Car Loan", 6.5);
    
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

