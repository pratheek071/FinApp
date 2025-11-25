package com.example.finapp.presentation.auth

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
import com.example.finapp.R
import com.example.finapp.data.model.UserRole
import com.example.finapp.databinding.FragmentPhoneAuthBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhoneAuthFragment : Fragment() {
    
    private var _binding: FragmentPhoneAuthBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AuthViewModel by viewModels()
    private val args: PhoneAuthFragmentArgs by navArgs()
    
    private var verificationId: String? = null
    private var phoneNumber: String = ""
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneAuthBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.tvSubtitle.text = "Sign in as ${args.role}"
        
        setupClickListeners()
        observeAuthState()
    }
    
    private fun setupClickListeners() {
        binding.btnSendOtp.setOnClickListener {
            phoneNumber = binding.etPhoneNumber.text.toString()
            if (phoneNumber.isNotEmpty()) {
                viewModel.sendOTP(phoneNumber, requireActivity())
            } else {
                Toast.makeText(context, "Please enter phone number", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.btnVerifyOtp.setOnClickListener {
            val otp = binding.etOtp.text.toString()
            if (otp.isNotEmpty() && verificationId != null) {
                viewModel.verifyOTP(verificationId!!, otp)
            } else {
                Toast.makeText(context, "Please enter OTP", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.tvResendOtp.setOnClickListener {
            viewModel.sendOTP(phoneNumber, requireActivity())
        }
        
        binding.btnSubmitProfile.setOnClickListener {
            val name = binding.etName.text.toString()
            if (name.isNotEmpty()) {
                val role = UserRole.valueOf(args.role)
                viewModel.createOrGetUser(phoneNumber, name, role)
            } else {
                Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun observeAuthState() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> showLoading(true)
                
                is AuthState.OTPSent -> {
                    showLoading(false)
                    verificationId = state.verificationId
                    showOTPInput()
                    Toast.makeText(context, "OTP sent successfully", Toast.LENGTH_SHORT).show()
                }
                
                is AuthState.OTPVerified -> {
                    showLoading(false)
                    showNameInput()
                }
                
                is AuthState.Authenticated -> {
                    showLoading(false)
                    navigateToDashboard(state.user.role)
                }
                
                is AuthState.Error -> {
                    showLoading(false)
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
        binding.btnSendOtp.isEnabled = !show
        binding.btnVerifyOtp.isEnabled = !show
        binding.btnSubmitProfile.isEnabled = !show
    }
    
    private fun showOTPInput() {
        binding.layoutPhoneInput.isVisible = false
        binding.layoutOtpInput.isVisible = true
        binding.layoutNameInput.isVisible = false
    }
    
    private fun showNameInput() {
        binding.layoutPhoneInput.isVisible = false
        binding.layoutOtpInput.isVisible = false
        binding.layoutNameInput.isVisible = true
    }
    
    private fun navigateToDashboard(role: UserRole) {
        val action = when (role) {
            UserRole.CLIENT -> PhoneAuthFragmentDirections.actionPhoneAuthToClientDashboard()
            UserRole.ADMIN -> PhoneAuthFragmentDirections.actionPhoneAuthToAdminDashboard()
        }
        findNavController().navigate(action)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

