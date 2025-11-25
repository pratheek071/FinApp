package com.example.finapp.data.repository

import com.example.finapp.data.model.Loan
import com.example.finapp.data.model.LoanStatus
import com.example.finapp.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    suspend fun createLoan(loan: Loan): Result<String> {
        return try {
            val loanRef = firestore.collection(Constants.COLLECTION_LOANS).document()
            val loanWithId = loan.copy(id = loanRef.id)
            loanRef.set(loanWithId).await()
            Result.success(loanRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateLoan(loan: Loan): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_LOANS)
                .document(loan.id)
                .set(loan)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateLoanStatus(loanId: String, status: LoanStatus): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status.name
            )
            
            // If approving, set approval timestamp and start/end dates
            if (status == LoanStatus.APPROVED) {
                val now = System.currentTimeMillis()
                updates["approvedAt"] = now
                updates["startDate"] = now
                
                // Get loan to calculate end date
                val loan = getLoan(loanId).getOrThrow()
                updates["endDate"] = now + (loan.totalDays * 24 * 60 * 60 * 1000L)
                updates["remainingAmount"] = loan.totalAmount
            }
            
            firestore.collection(Constants.COLLECTION_LOANS)
                .document(loanId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getLoan(loanId: String): Result<Loan> {
        return try {
            val doc = firestore.collection(Constants.COLLECTION_LOANS)
                .document(loanId)
                .get()
                .await()
            
            val loan = doc.toObject(Loan::class.java) 
                ?: throw Exception("Loan not found")
            
            Result.success(loan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserLoans(userId: String): Result<List<Loan>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_LOANS)
                .whereEqualTo("userId", userId)
                .orderBy("requestedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val loans = snapshot.documents.mapNotNull { 
                it.toObject(Loan::class.java) 
            }
            
            Result.success(loans)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPendingLoans(): Result<List<Loan>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_LOANS)
                .whereEqualTo("status", LoanStatus.PENDING.name)
                .orderBy("requestedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val loans = snapshot.documents.mapNotNull { 
                it.toObject(Loan::class.java) 
            }
            
            Result.success(loans)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getApprovedLoans(): Result<List<Loan>> {
        return try {
            // Simple query without orderBy to avoid requiring a composite index during development
            val snapshot = firestore.collection(Constants.COLLECTION_LOANS)
                .whereEqualTo("status", LoanStatus.APPROVED.name)
                .get()
                .await()
            
            val loans = snapshot.documents.mapNotNull { 
                it.toObject(Loan::class.java) 
            }
            
            Result.success(loans)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllLoans(): Result<List<Loan>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_LOANS)
                .orderBy("requestedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val loans = snapshot.documents.mapNotNull { 
                it.toObject(Loan::class.java) 
            }
            
            Result.success(loans)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun observeUserLoans(userId: String): Flow<List<Loan>> = callbackFlow {
        val listenerRegistration = firestore.collection(Constants.COLLECTION_LOANS)
            .whereEqualTo("userId", userId)
            .orderBy("requestedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val loans = snapshot?.documents?.mapNotNull { 
                    it.toObject(Loan::class.java) 
                } ?: emptyList()
                
                trySend(loans)
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    fun observePendingLoans(): Flow<List<Loan>> = callbackFlow {
        val listenerRegistration = firestore.collection(Constants.COLLECTION_LOANS)
            .whereEqualTo("status", LoanStatus.PENDING.name)
            .orderBy("requestedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val loans = snapshot?.documents?.mapNotNull { 
                    it.toObject(Loan::class.java) 
                } ?: emptyList()
                
                trySend(loans)
            }
        
        awaitClose { listenerRegistration.remove() }
    }
}

