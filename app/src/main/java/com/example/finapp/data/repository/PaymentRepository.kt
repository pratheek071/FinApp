package com.example.finapp.data.repository

import com.example.finapp.data.model.DailyPaymentSummary
import com.example.finapp.data.model.Payment
import com.example.finapp.data.model.PaymentStatus
import com.example.finapp.utils.Constants
import com.example.finapp.utils.DateUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val loanRepository: LoanRepository
) {
    
    suspend fun createPayment(payment: Payment): Result<String> {
        return try {
            val paymentRef = firestore.collection(Constants.COLLECTION_PAYMENTS).document()
            val paymentWithId = payment.copy(id = paymentRef.id)
            
            // Start a batch write
            val batch = firestore.batch()
            
            // Add payment
            batch.set(paymentRef, paymentWithId)
            
            // Update loan's paid amount and remaining amount
            val loan = loanRepository.getLoan(payment.loanId).getOrThrow()
            val newPaidAmount = loan.paidAmount + payment.amount
            val newRemainingAmount = loan.totalAmount - newPaidAmount
            
            val loanRef = firestore.collection(Constants.COLLECTION_LOANS)
                .document(payment.loanId)
            
            val loanUpdates = hashMapOf<String, Any>(
                "paidAmount" to newPaidAmount,
                "remainingAmount" to newRemainingAmount
            )
            
            // If fully paid, mark as completed
            if (newRemainingAmount <= 0) {
                loanUpdates["status"] = "COMPLETED"
            }
            
            batch.update(loanRef, loanUpdates)
            
            // Commit the batch
            batch.commit().await()
            
            Result.success(paymentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPayment(paymentId: String): Result<Payment> {
        return try {
            val doc = firestore.collection(Constants.COLLECTION_PAYMENTS)
                .document(paymentId)
                .get()
                .await()
            
            val payment = doc.toObject(Payment::class.java) 
                ?: throw Exception("Payment not found")
            
            Result.success(payment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getLoanPayments(loanId: String): Result<List<Payment>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_PAYMENTS)
                .whereEqualTo("loanId", loanId)
                .orderBy("paymentDate", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val payments = snapshot.documents.mapNotNull { 
                it.toObject(Payment::class.java) 
            }
            
            Result.success(payments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserPayments(userId: String): Result<List<Payment>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_PAYMENTS)
                .whereEqualTo("userId", userId)
                .orderBy("paymentDate", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val payments = snapshot.documents.mapNotNull { 
                it.toObject(Payment::class.java) 
            }
            
            Result.success(payments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTodayPayments(): Result<List<Payment>> {
        return try {
            val startOfDay = DateUtils.getStartOfDay()
            val endOfDay = DateUtils.getEndOfDay()
            
            val snapshot = firestore.collection(Constants.COLLECTION_PAYMENTS)
                .whereGreaterThanOrEqualTo("paymentDate", startOfDay)
                .whereLessThanOrEqualTo("paymentDate", endOfDay)
                .whereEqualTo("status", PaymentStatus.SUCCESS.name)
                .orderBy("paymentDate", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val payments = snapshot.documents.mapNotNull { 
                it.toObject(Payment::class.java) 
            }
            
            Result.success(payments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getDailyPaymentSummary(): Result<DailyPaymentSummary> {
        return try {
            val payments = getTodayPayments().getOrThrow()
            
            val totalAmount = payments.sumOf { it.amount }
            val customerCount = payments.distinctBy { it.userId }.size
            
            val summary = DailyPaymentSummary(
                date = System.currentTimeMillis(),
                totalAmount = totalAmount,
                totalPaid = totalAmount,
                customerCount = customerCount,
                payments = payments
            )
            
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTodayPaymentForLoan(loanId: String): Result<Payment?> {
        return try {
            val startOfDay = DateUtils.getStartOfDay()
            val endOfDay = DateUtils.getEndOfDay()
            
            val snapshot = firestore.collection(Constants.COLLECTION_PAYMENTS)
                .whereEqualTo("loanId", loanId)
                .whereGreaterThanOrEqualTo("paymentDate", startOfDay)
                .whereLessThanOrEqualTo("paymentDate", endOfDay)
                .limit(1)
                .get()
                .await()
            
            val payment = snapshot.documents.firstOrNull()?.toObject(Payment::class.java)
            
            Result.success(payment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

