package com.lifestyler.android.presentation.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lifestyler.android.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    @javax.inject.Inject
    lateinit var pollingManager: com.lifestyler.android.data.manager.PollingManager

    private lateinit var binding: com.lifestyler.android.databinding.ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Set the layout FIRST with Binding
        binding = com.lifestyler.android.databinding.ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 2. Enable immersive edge-to-edge
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        
        // 3. Handle status bar icon colors (Dark icons for Light background)
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)?.let { controller ->
            controller.isAppearanceLightStatusBars = true
        }
        
        // 4. Initialize Polling!
        pollingManager.setupPollingWorker()
        
        // 5. Auto-login check
        val preferenceManager = com.lifestyler.android.data.preference.PreferenceManager(this)
        if (preferenceManager.getSheetName() != null) {
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
} 