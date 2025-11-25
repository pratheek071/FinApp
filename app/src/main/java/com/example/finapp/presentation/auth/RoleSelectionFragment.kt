package com.example.finapp.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.finapp.R
import com.example.finapp.databinding.FragmentRoleSelectionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoleSelectionFragment : Fragment() {
    
    private var _binding: FragmentRoleSelectionBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoleSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnClient.setOnClickListener {
            navigateToPhoneAuth("CLIENT")
        }
        
        binding.btnAdmin.setOnClickListener {
            navigateToPhoneAuth("ADMIN")
        }
    }
    
    private fun navigateToPhoneAuth(role: String) {
        val action = RoleSelectionFragmentDirections
            .actionRoleSelectionToPhoneAuth(role)
        findNavController().navigate(action)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

