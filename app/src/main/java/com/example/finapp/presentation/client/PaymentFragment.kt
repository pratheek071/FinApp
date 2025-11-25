package com.example.finapp.presentation.client

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.finapp.data.model.Payment
import com.example.finapp.data.model.PaymentStatus
import com.example.finapp.databinding.FragmentPaymentBinding
import com.example.finapp.utils.Constants
import com.example.finapp.utils.PreferenceManager
import com.example.finapp.utils.UpiPaymentHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PaymentFragment : Fragment() {
    
    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PaymentViewModel by viewModels()
    private val loanViewModel: LoanViewModel by viewModels()
    private val args: PaymentFragmentArgs by navArgs()
    
    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    private var currentLoan: com.example.finapp.data.model.Loan? = null
    private var pendingPaymentAmount: Double = 0.0
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
        observeViewModels()
        
        // Load loan details
        loanViewModel.getLoan(args.loanId)
        
        // Check if payment is already made today
        viewModel.checkTodayPayment(args.loanId)
        
        // Display admin UPI ID
        binding.tvAdminUpiId.text = "${Constants.PAYMENT_RECEIVER_NAME}\n${Constants.ADMIN_UPI_ID}"
    }
    
    private fun setupClickListeners() {
        // Pay with UPI button - Opens UPI apps
        binding.btnPayWithUpi.setOnClickListener {
            initiateUpiPayment()
        }
        
        // Manual payment confirmation
        binding.btnConfirmPayment.setOnClickListener {
            submitManualPayment()
        }
    }
    
    private fun initiateUpiPayment() {
        val loan = currentLoan ?: return
        
        UpiPaymentHelper.initiateUpiPayment(
            activity = requireActivity(),
            payeeUpiId = Constants.ADMIN_UPI_ID,
            payeeName = Constants.PAYMENT_RECEIVER_NAME,
            transactionNote = "Loan Payment - ${loan.loanType.displayName}",
            amount = loan.dailyAmount
        )
        
        pendingPaymentAmount = loan.dailyAmount
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == UpiPaymentHelper.UPI_PAYMENT_REQUEST_CODE) {
            val paymentResult = UpiPaymentHelper.handlePaymentResult(resultCode, data)
            handleUpiPaymentResult(paymentResult)
        }
    }
    
    private fun handleUpiPaymentResult(result: com.example.finapp.utils.UpiPaymentResult) {
        when (result.status) {
            com.example.finapp.utils.PaymentStatus.SUCCESS -> {
                // Payment successful, record it
                val transactionId = result.txnId ?: result.txnRef ?: "UPI_${System.currentTimeMillis()}"
                recordPayment(transactionId, "")
                Toast.makeText(context, "Payment successful!", Toast.LENGTH_LONG).show()
            }
            
            com.example.finapp.utils.PaymentStatus.PENDING -> {
                Toast.makeText(
                    context,
                    "Payment is pending. Please check your UPI app",
                    Toast.LENGTH_LONG
                ).show()
            }
            
            com.example.finapp.utils.PaymentStatus.FAILED -> {
                Toast.makeText(
                    context,
                    "Payment failed: ${result.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun submitManualPayment() {
        val transactionId = binding.etTransactionId.text.toString()
        
        if (transactionId.isEmpty()) {
            Toast.makeText(context, "Please enter transaction ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        val upiId = binding.etUpiId.text.toString()
        recordPayment(transactionId, upiId)
    }
    
    private fun recordPayment(transactionId: String, upiId: String) {
        val loan = currentLoan ?: return
        val userId = preferenceManager.userId ?: return
        val userName = preferenceManager.userName ?: return
        
        // Calculate day number
        val startDate = loan.startDate ?: System.currentTimeMillis()
        val currentDate = System.currentTimeMillis()
        val dayNumber = ((currentDate - startDate) / (1000 * 60 * 60 * 24)).toInt() + 1
        
        val payment = Payment(
            loanId = loan.id,
            userId = userId,
            userName = userName,
            amount = loan.dailyAmount,
            paymentDate = System.currentTimeMillis(),
            status = PaymentStatus.SUCCESS,
            transactionId = transactionId,
            upiId = upiId,
            dayNumber = dayNumber
        )
        
        viewModel.submitPayment(payment)
    }
    
    private fun observeViewModels() {
        loanViewModel.loanState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoanState.LoanLoaded -> {
                    currentLoan = state.loan
                    displayLoanDetails(state.loan)
                }
                is LoanState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
        
        viewModel.todayPayment.observe(viewLifecycleOwner) { payment ->
            if (payment != null) {
                // Payment already made today
                showPaymentCompleted()
            }
        }
        
        viewModel.paymentState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PaymentState.Loading -> showLoading(true)
                
                is PaymentState.Success -> {
                    showLoading(false)
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
                
                is PaymentState.Error -> {
                    showLoading(false)
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                
                else -> showLoading(false)
            }
        }
    }
    
    private fun displayLoanDetails(loan: com.example.finapp.data.model.Loan) {
        binding.apply {
            tvLoanType.text = loan.loanType.displayName
            tvTotalAmount.text = "₹${loan.totalAmount}"
            tvPaidAmount.text = "₹${loan.paidAmount}"
            tvRemainingAmount.text = "₹${loan.remainingAmount}"
            tvDailyAmount.text = "₹${loan.dailyAmount}"
        }
    }
    
    private fun showPaymentCompleted() {
        binding.btnPayWithUpi.isEnabled = false
        binding.etUpiId.isEnabled = false
        binding.etTransactionId.isEnabled = false
        binding.btnConfirmPayment.isEnabled = false
        binding.tvPaymentStatus.isVisible = true
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
        binding.btnPayWithUpi.isEnabled = !show
        binding.btnConfirmPayment.isEnabled = !show
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

