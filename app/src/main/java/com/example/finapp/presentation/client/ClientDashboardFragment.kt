package com.example.finapp.presentation.client

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.finapp.R
import com.example.finapp.databinding.FragmentClientDashboardBinding
import com.example.finapp.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClientDashboardFragment : Fragment() {
    
    private var _binding: FragmentClientDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: LoanViewModel by viewModels()
    
    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    
    private lateinit var loansAdapter: LoansAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClickListeners()
        setupMenu()
        observeViewModel()
        
        loadLoans()
    }
    
    private fun setupRecyclerView() {
        loansAdapter = LoansAdapter { loan ->
            navigateToPayment(loan.id)
        }
        binding.rvLoans.adapter = loansAdapter
    }
    
    private fun setupClickListeners() {
        binding.fabAddLoan.setOnClickListener {
            findNavController().navigate(R.id.action_clientDashboard_to_loanApplication)
        }
    }
    
    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_dashboard, menu)
            }
            
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_logout -> {
                        logout()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    
    private fun loadLoans() {
        val userId = preferenceManager.userId ?: return
        viewModel.loadUserLoans(userId)
    }
    
    private fun observeViewModel() {
        viewModel.userLoans.observe(viewLifecycleOwner) { loans ->
            loansAdapter.submitList(loans)
            binding.layoutEmptyState.isVisible = loans.isEmpty()
            binding.rvLoans.isVisible = loans.isNotEmpty()
        }
        
        viewModel.loanState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoanState.Loading -> showLoading(true)
                is LoanState.LoansLoaded -> showLoading(false)
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
    }
    
    private fun navigateToPayment(loanId: String) {
        val action = ClientDashboardFragmentDirections.actionClientDashboardToPayment(loanId)
        findNavController().navigate(action)
    }
    
    private fun logout() {
        firebaseAuth.signOut()
        preferenceManager.clear()

        // Clear client dashboard from back stack so back doesn't return here
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.clientDashboardFragment, true)
            .build()

        findNavController().navigate(R.id.roleSelectionFragment, null, navOptions)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

