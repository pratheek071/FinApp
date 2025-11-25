package com.example.finapp.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class UpiPaymentHelper {
    
    companion object {
        const val UPI_PAYMENT_REQUEST_CODE = 1001
        
        /**
         * Initiates UPI payment by opening UPI apps installed on device
         *
         * Strategy:
         * 1. Try to open PhonePe directly (most common case).
         * 2. If that fails, fall back to a generic UPI chooser.
         */
        fun initiateUpiPayment(
            activity: Activity,
            payeeUpiId: String,
            payeeName: String,
            transactionNote: String,
            amount: Double
        ) {
            try {
                // Generate unique transaction reference ID
                val transactionRefId = generateTransactionId()
                
                // Build UPI URI
                val upiUri = Uri.parse(
                    buildUpiString(
                        payeeUpiId = payeeUpiId,
                        payeeName = payeeName,
                        transactionRefId = transactionRefId,
                        transactionNote = transactionNote,
                        amount = amount
                    )
                )
                
                // 1) Try PhonePe explicitly first (common real-world case)
                val phonePeIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = upiUri
                    // Standard PhonePe package name
                    setPackage("com.phonepe.app")
                }
                
                try {
                    activity.startActivityForResult(phonePeIntent, UPI_PAYMENT_REQUEST_CODE)
                    return
                } catch (_: ActivityNotFoundException) {
                    // PhonePe not installed with this package name – fall back to generic approach
                }
                
                // 2) Generic UPI chooser – let the system show any UPI-capable app (GPay/Paytm/etc.)
                val genericIntent = Intent(Intent.ACTION_VIEW, upiUri)
                val chooser = Intent.createChooser(genericIntent, "Pay using UPI")
                
                try {
                    activity.startActivityForResult(chooser, UPI_PAYMENT_REQUEST_CODE)
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(
                        activity,
                        "No UPI app found. Please install PhonePe, GPay, Paytm or any UPI app.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    activity,
                    "Error initiating payment: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        
        /**
         * Builds UPI payment string according to UPI specifications
         */
        private fun buildUpiString(
            payeeUpiId: String,
            payeeName: String,
            transactionRefId: String,
            transactionNote: String,
            amount: Double
        ): String {
            return "upi://pay?pa=$payeeUpiId" +
                    "&pn=${Uri.encode(payeeName)}" +
                    "&tr=$transactionRefId" +
                    "&tn=${Uri.encode(transactionNote)}" +
                    "&am=$amount" +
                    "&cu=INR"
        }
        
        /**
         * Generates unique transaction reference ID
         */
        private fun generateTransactionId(): String {
            val timestamp = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
            return "FINAPP${dateFormat.format(Date(timestamp))}"
        }
        
        /**
         * Handles UPI payment result from onActivityResult
         * 
         * @return UpiPaymentResult with status and data
         */
        fun handlePaymentResult(resultCode: Int, data: Intent?): UpiPaymentResult {
            return try {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val response = data.getStringExtra("response") ?: ""
                    parseUpiResponse(response)
                } else {
                    UpiPaymentResult(
                        status = PaymentStatus.FAILED,
                        message = "Payment cancelled by user",
                        txnId = null,
                        txnRef = null
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                UpiPaymentResult(
                    status = PaymentStatus.FAILED,
                    message = "Error processing payment: ${e.message}",
                    txnId = null,
                    txnRef = null
                )
            }
        }
        
        /**
         * Parses UPI response string
         * Response format: txnId=xxx&responseCode=xx&Status=xxx&txnRef=xxx
         */
        private fun parseUpiResponse(response: String): UpiPaymentResult {
            val params = response.split("&").associate {
                val parts = it.split("=")
                if (parts.size == 2) parts[0].lowercase() to parts[1] else parts[0] to ""
            }
            
            val status = params["status"]?.lowercase() ?: ""
            val responseCode = params["responsecode"] ?: ""
            val txnId = params["txnid"]
            val txnRef = params["txnref"]
            val approvalRefNo = params["approvalrefno"]
            
            return when {
                status == "success" || responseCode == "00" -> {
                    UpiPaymentResult(
                        status = PaymentStatus.SUCCESS,
                        message = "Payment successful",
                        txnId = txnId ?: approvalRefNo,
                        txnRef = txnRef
                    )
                }
                status == "submitted" || status == "pending" -> {
                    UpiPaymentResult(
                        status = PaymentStatus.PENDING,
                        message = "Payment is pending",
                        txnId = txnId,
                        txnRef = txnRef
                    )
                }
                else -> {
                    UpiPaymentResult(
                        status = PaymentStatus.FAILED,
                        message = "Payment failed",
                        txnId = txnId,
                        txnRef = txnRef
                    )
                }
            }
        }
    }
}

/**
 * Result of UPI payment transaction
 */
data class UpiPaymentResult(
    val status: PaymentStatus,
    val message: String,
    val txnId: String?,
    val txnRef: String?
)

enum class PaymentStatus {
    SUCCESS,
    PENDING,
    FAILED
}

