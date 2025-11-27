package com.example.finapp.presentation.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finapp.R
import com.example.finapp.data.model.Loan
import com.example.finapp.data.model.LoanStatus
import com.example.finapp.data.model.LoanType
import com.example.finapp.databinding.FragmentLoanApplicationBinding
import com.example.finapp.utils.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoanApplicationFragment : Fragment() {
    
    private var _binding: FragmentLoanApplicationBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: LoanViewModel by viewModels()
    
    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    private var selectedLoanType: LoanType = LoanType.EDUCATION
    
    companion object {
        private const val MIN_LOAN_AMOUNT = 10000.0
        private const val MAX_LOAN_AMOUNT = 20000000.0
        private const val MIN_DURATION_YEARS = 2
        private const val MAX_DURATION_YEARS = 25
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoanApplicationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupUI() {
        // Auto-fill user details
        binding.etName.setText(preferenceManager.userName)
        binding.etPhone.setText(preferenceManager.userPhone)
        
        // Setup loan type spinner
        val loanTypes = LoanType.values().map { it.displayName }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            loanTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLoanType.adapter = adapter
        
        // Update interest rate when loan type changes
        binding.spinnerLoanType.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedLoanType = LoanType.values()[position]
                binding.tvInterestRate.text = "${selectedLoanType.interestRate}%"
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }
    
    private fun setupClickListeners() {
        binding.btnCalculate.setOnClickListener {
            calculateLoan()
        }
        
        binding.btnSubmitLoan.setOnClickListener {
            submitLoan()
        }
    }
    
    private fun calculateLoan() {
        val amountStr = binding.etLoanAmount.text.toString()
        val durationStr = binding.etDuration.text.toString()
        
        if (amountStr.isEmpty() || durationStr.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        val amount = amountStr.toDoubleOrNull()
        val durationYears = durationStr.toIntOrNull()
        
        // Validate amount
        if (amount == null || amount <= 0) {
            Toast.makeText(context, "Please enter valid amount", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (amount < MIN_LOAN_AMOUNT) {
            Toast.makeText(
                context, 
                "Loan amount must be at least ₹${MIN_LOAN_AMOUNT.toInt()}", 
                Toast.LENGTH_LONG
            ).show()
            binding.etLoanAmount.error = "Minimum ₹${MIN_LOAN_AMOUNT.toInt()}"
            return
        }
        
        if (amount > MAX_LOAN_AMOUNT) {
            Toast.makeText(
                context, 
                "Loan amount cannot exceed ₹${MAX_LOAN_AMOUNT.toInt()}", 
                Toast.LENGTH_LONG
            ).show()
            binding.etLoanAmount.error = "Maximum ₹${MAX_LOAN_AMOUNT.toInt()}"
            return
        }
        
        // Validate duration
        if (durationYears == null || durationYears <= 0) {
            Toast.makeText(context, "Please enter valid duration in years", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (durationYears < MIN_DURATION_YEARS) {
            Toast.makeText(
                context, 
                "Loan duration must be at least $MIN_DURATION_YEARS years", 
                Toast.LENGTH_LONG
            ).show()
            binding.etDuration.error = "Minimum $MIN_DURATION_YEARS years"
            return
        }
        
        if (durationYears > MAX_DURATION_YEARS) {
            Toast.makeText(
                context, 
                "Loan duration cannot exceed $MAX_DURATION_YEARS years", 
                Toast.LENGTH_LONG
            ).show()
            binding.etDuration.error = "Maximum $MAX_DURATION_YEARS years"
            return
        }
        
        // Clear any previous errors
        binding.etLoanAmount.error = null
        binding.etDuration.error = null
        
        viewModel.calculateLoan(amount, selectedLoanType.interestRate, durationYears)
    }
    
    private fun submitLoan() {
        val userId = preferenceManager.userId ?: return
        val userName = preferenceManager.userName ?: return
        val userPhone = preferenceManager.userPhone ?: return
        
        val calculation = viewModel.loanCalculation.value ?: return
        
        val loan = Loan(
            userId = userId,
            userName = userName,
            phoneNumber = userPhone,
            loanType = selectedLoanType,
            interestRate = selectedLoanType.interestRate,
            principalAmount = calculation.principalAmount,
            durationMonths = calculation.durationMonths,
            totalAmount = calculation.totalAmount,
            dailyAmount = calculation.dailyAmount,
            totalDays = calculation.durationDays,
            status = LoanStatus.PENDING,
            remainingAmount = calculation.totalAmount
        )
        
        viewModel.submitLoan(loan)
    }
    
    private fun observeViewModel() {
        viewModel.loanCalculation.observe(viewLifecycleOwner) { calculation ->
            binding.cardResult.isVisible = true
            binding.btnSubmitLoan.isVisible = true
            
            binding.tvPrincipalAmount.text = "₹${calculation.principalAmount}"
            binding.tvInterestAmount.text = "₹${calculation.totalInterest}"
            binding.tvTotalAmount.text = "₹${calculation.totalAmount}"
            binding.tvDailyAmount.text = "₹${calculation.monthlyAmount}"
        }
        
        viewModel.loanState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoanState.Loading -> showLoading(true)
                
                is LoanState.Success -> {
                    showLoading(false)
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
                
                is LoanState.Error -> {
                    showLoading(false)
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                
                else -> showLoading(false)
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
        binding.btnCalculate.isEnabled = !show
        binding.btnSubmitLoan.isEnabled = !show
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

