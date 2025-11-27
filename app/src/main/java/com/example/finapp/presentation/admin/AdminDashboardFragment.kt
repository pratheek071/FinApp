package com.example.finapp.presentation.admin

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
import com.example.finapp.databinding.FragmentAdminDashboardBinding
import com.example.finapp.utils.PreferenceManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AdminDashboardFragment : Fragment() {
    
    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AdminViewModel by viewModels()
    
    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    
    private lateinit var pendingLoansAdapter: AdminLoansAdapter
    private lateinit var activeLoansAdapter: AdminLoansAdapter
    
    private var currentTab = 0
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupTabs()
        setupMenu()
        observeViewModel()
        
        loadData()
    }
    
    private fun setupRecyclerView() {
        pendingLoansAdapter = AdminLoansAdapter(
            onApprove = { loan -> viewModel.approveLoan(loan.id) },
            onReject = { loan -> viewModel.rejectLoan(loan.id) },
            showActions = true
        )
        
        activeLoansAdapter = AdminLoansAdapter(
            onApprove = {},
            onReject = {},
            showActions = false
        )
        
        binding.rvLoans.adapter = pendingLoansAdapter
    }
    
    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        currentTab = 0
                        binding.rvLoans.adapter = pendingLoansAdapter
                        viewModel.loadPendingLoans()
                    }
                    1 -> {
                        currentTab = 1
                        binding.rvLoans.adapter = activeLoansAdapter
                        viewModel.loadActiveLoans()
                    }
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
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
    
    private fun loadData() {
        viewModel.loadPendingLoans()
        viewModel.loadDailySummary()
    }
    
    private fun observeViewModel() {
        viewModel.pendingLoans.observe(viewLifecycleOwner) { loans ->
            pendingLoansAdapter.submitList(loans)
            updateEmptyState(loans.isEmpty() && currentTab == 0)
        }
        
        viewModel.activeLoans.observe(viewLifecycleOwner) { loans ->
            activeLoansAdapter.submitList(loans)
            updateEmptyState(loans.isEmpty() && currentTab == 1)
        }
        
        viewModel.dailySummary.observe(viewLifecycleOwner) { summary ->
            binding.tvTotalCollected.text = "₹${summary.totalAmount}"
            binding.tvCustomersPaid.text = "${summary.customerCount}"
        }
        
        viewModel.adminState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AdminState.Loading -> showLoading(true)
                
                is AdminState.PendingLoansLoaded,
                is AdminState.ActiveLoansLoaded -> showLoading(false)
                
                is AdminState.Success -> {
                    showLoading(false)
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                
                is AdminState.Error -> {
                    showLoading(false)
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmptyState.isVisible = isEmpty
        binding.rvLoans.isVisible = !isEmpty
    }
    
    private fun logout() {
        firebaseAuth.signOut()
        preferenceManager.clear()

        // Clear admin dashboard from back stack so back doesn't return here
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.adminDashboardFragment, true)
            .build()

        findNavController().navigate(R.id.roleSelectionFragment, null, navOptions)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

