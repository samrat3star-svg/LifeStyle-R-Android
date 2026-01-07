package com.lifestyler.android.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.lifestyler.android.R
import com.lifestyler.android.data.worker.FastingPollingWorker
import com.lifestyler.android.data.preference.PreferenceManager
import com.lifestyler.android.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    @javax.inject.Inject
    lateinit var pollingManager: com.lifestyler.android.data.manager.PollingManager
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Set the layout FIRST
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferenceManager = PreferenceManager(this)
        
        // 2. Enable edge-to-edge (Modern & Robust)
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        supportActionBar?.hide() 
        
        // 3. Handle status bar icon colors (Light icons for Dark/Vibrant background)
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)?.let { controller ->
            controller.isAppearanceLightStatusBars = false
        }

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
        checkNotificationPermission()
        setupNavigation()
        setupPollingWorker()
    }

    private fun checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    fun setupPollingWorker(force: Boolean = false) {
        pollingManager.setupPollingWorker(force)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)
        
        // Hide bottom navigation for specific fragments (e.g., Fasting Dashboard)
        // Always show bottom navigation
        binding.bottomNavigationView.visibility = android.view.View.VISIBLE
        
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // You can add logic here if you need to hide it on specific detail screens
            // For now, visible everywhere
        }
    }
} 