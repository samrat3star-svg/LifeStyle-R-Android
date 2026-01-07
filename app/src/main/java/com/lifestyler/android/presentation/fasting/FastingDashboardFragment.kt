package com.lifestyler.android.presentation.fasting

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.lifestyler.android.R
import com.lifestyler.android.databinding.FragmentFastingDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FastingDashboardFragment : Fragment() {

    private var _binding: FragmentFastingDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: FastingDashboardViewModel by viewModels()
    private var sheetName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFastingDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var followClickCount = 0
    private var breakClickCount = 0
    private var lastClickTime: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sheetName = arguments?.getString("sheetName") ?: com.lifestyler.android.data.preference.PreferenceManager(requireContext()).getSheetName()
        
        if (sheetName != null) {
            viewModel.loadSettings(sheetName!!)
        } else {
            Toast.makeText(context, "Error: Client Sheet Name missing", Toast.LENGTH_LONG).show()
        }

        binding.syncFab.setOnClickListener {
            binding.syncFab.isEnabled = false // Optimistic disable
            binding.syncFab.alpha = 0.5f
            viewModel.manualSync()
        }

        binding.followButton.setOnClickListener {
            followClickCount++
            android.util.Log.d("FastingDashboard", "FOLLOW Button Clicked! Total: $followClickCount")
            
            if (sheetName != null) {
                binding.followButton.isEnabled = false
                binding.followButton.alpha = 0.5f
                val duration = binding.hoursText.text.toString()
                viewModel.logFasting(sheetName!!, duration.ifBlank { "Completed" })
            }
        }

        binding.breakFastingButton.setOnClickListener {
            breakClickCount++
            android.util.Log.d("FastingDashboard", "BREAK Button Clicked! Total: $breakClickCount")
            
            binding.breakFastingButton.isEnabled = false
            binding.breakFastingButton.alpha = 0.5f
            viewModel.breakFasting()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                updateUI(state)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pollingState.collect { polling ->
                updatePollingUI(polling)
            }
        }
    }

    private fun updatePollingUI(polling: FastingDashboardViewModel.PollingState) {
        if (_binding == null) return
        binding.syncStatusContainer.visibility = if (polling.isVisible) View.VISIBLE else View.GONE
        
        if (polling.errorMessage != null) {
            binding.syncStatusText.text = "Sync failed. ${polling.nextSyncTime}"
            binding.syncStatusText.setTextColor(android.graphics.Color.parseColor("#FF6B6B")) // Soft red
        } else {
            binding.syncStatusText.text = "Next check in: ${polling.nextSyncTime}"
            binding.syncStatusText.setTextColor(android.graphics.Color.WHITE)
        }
        
        binding.lastSyncText.text = "Last check: ${polling.lastSyncTime}"
    }

    private fun updateUI(state: FastingDashboardViewModel.DashboardState) {
        android.util.Log.d("FastingDashboard", "updateUI: isLoading=${state.isLoading}, isFollowed=${state.isFollowedToday}")
        if (state.isLoading) {
            android.util.Log.d("FastingDashboard", "updateUI: DISABLING buttons")
            binding.followButton.isEnabled = false
            binding.followButton.alpha = 0.5f
            binding.breakFastingButton.isEnabled = false
            binding.breakFastingButton.alpha = 0.5f
            binding.syncFab.isEnabled = false
            binding.syncFab.alpha = 0.5f
        } else {
            android.util.Log.d("FastingDashboard", "updateUI: ENABLING buttons")
            binding.followButton.isEnabled = true
            binding.followButton.alpha = 1.0f
            binding.breakFastingButton.isEnabled = true
            binding.breakFastingButton.alpha = 1.0f
            binding.syncFab.isEnabled = true
            binding.syncFab.alpha = 1.0f
        }

        binding.userNameText.text = state.userName
        binding.startTimeText.text = "Start: ${state.startTime}"
        binding.endTimeText.text = "End: ${state.endTime}"
        binding.hoursText.text = state.fastingHours

        if (state.errorMessage != null) {
            Toast.makeText(context, state.errorMessage, Toast.LENGTH_SHORT).show()
        }

        if (state.isFollowedToday) {
            binding.statusText.text = "Goal Achieved Today!"
            binding.statusCard.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#32CD32")))
            binding.statusCard.strokeWidth = 0
            binding.followButton.visibility = View.GONE
            binding.breakFastingButton.visibility = View.VISIBLE
        } else {
            binding.statusText.text = "Not Followed Yet"
            binding.statusCard.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#20FFFFFF")))
            binding.statusCard.strokeWidth = 1 
            binding.followButton.visibility = View.VISIBLE
            binding.breakFastingButton.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
