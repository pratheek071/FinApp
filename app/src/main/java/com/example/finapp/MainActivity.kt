package com.example.finapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.finapp.databinding.ActivityMainBinding
import com.example.finapp.utils.NotificationScheduler
import com.example.finapp.utils.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    @Inject
    lateinit var notificationScheduler: NotificationScheduler
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupNavigation()
        
        // Schedule daily reminders
        notificationScheduler.scheduleDailyReminders()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Check if user is already logged in
        if (preferenceManager.isLoggedIn()) {
            val startDestination = when (preferenceManager.userRole?.name) {
                "CLIENT" -> R.id.clientDashboardFragment
                "ADMIN" -> R.id.adminDashboardFragment
                else -> R.id.roleSelectionFragment
            }
            
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
            navGraph.setStartDestination(startDestination)
            navController.graph = navGraph
        }
    }
}