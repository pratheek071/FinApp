package com.example.finapp.data.model

data class Payment(
    val id: String = "",
    val loanId: String = "",
    val userId: String = "",
    val userName: String = "",
    val amount: Double = 0.0,
    val paymentDate: Long = System.currentTimeMillis(),
    val status: PaymentStatus = PaymentStatus.SUCCESS,
    val transactionId: String = "",
    val upiId: String = "",
    val dayNumber: Int = 0,
    val paymentMethod: String = "UPI"
)

enum class PaymentStatus {
    SUCCESS,
    PENDING,
    FAILED
}

data class DailyPaymentSummary(
    val date: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0,
    val totalPaid: Double = 0.0,
    val customerCount: Int = 0,
    val payments: List<Payment> = emptyList()
)

