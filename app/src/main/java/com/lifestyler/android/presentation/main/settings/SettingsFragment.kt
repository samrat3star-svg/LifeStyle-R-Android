package com.lifestyler.android.presentation.main.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lifestyler.android.databinding.FragmentSettingsBinding
import com.lifestyler.android.data.preference.PreferenceManager
import android.media.RingtoneManager
import android.net.Uri
import android.app.Activity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    @javax.inject.Inject
    lateinit var pollingManager: com.lifestyler.android.data.manager.PollingManager
    private lateinit var preferenceManager: PreferenceManager
    
    private val ringtoneLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                val ringtone = RingtoneManager.getRingtone(requireContext(), uri)
                val name = ringtone.getTitle(requireContext())
                
                preferenceManager.setAlarmRingtoneUri(uri.toString())
                preferenceManager.setAlarmRingtoneName(name)
                binding.selectedRingtoneText.text = name
            } else {
                // "Silent" selected or cancelled
                // Note: If "Silent" is picked, uri is null but resultCode is OK
                preferenceManager.setAlarmRingtoneUri("")
                preferenceManager.setAlarmRingtoneName("Silent")
                binding.selectedRingtoneText.text = "Silent"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferenceManager = PreferenceManager(requireContext())
        
        loadCurrentSettings()
        setupListeners()
    }

    private fun loadCurrentSettings() {
        binding.pollingEnabledSwitch.isChecked = preferenceManager.isPollingEnabled()
        binding.pollingIntervalEditText.setText(preferenceManager.getPollingInterval().toString())
        binding.alarmEnabledSwitch.isChecked = preferenceManager.isAlarmEnabled()
        
        val ringtoneName = preferenceManager.getAlarmRingtoneName() ?: "Default"
        binding.selectedRingtoneText.text = ringtoneName
    }

    private fun setupListeners() {
        binding.saveButton.setOnClickListener {
            saveSettings()
        }
        
        binding.selectedRingtoneText.setOnClickListener {
            launchRingtonePicker()
        }

        binding.testAlarmButton.setOnClickListener {
            val uri = preferenceManager.getAlarmRingtoneUri()
            com.lifestyler.android.presentation.util.AlarmNotificationHelper.triggerAlarm(
                requireContext(),
                uri,
                "Test Alarm: If you hear this, your notifications are working correctly! ✅"
            )
        }

        binding.logoutButton.setOnClickListener {
            val intent = android.content.Intent(context, com.lifestyler.android.presentation.auth.AuthActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            activity?.finish()
        }
    }
    
    private fun launchRingtonePicker() {
        val currentUriStr = preferenceManager.getAlarmRingtoneUri()
        val currentUri = if (!currentUriStr.isNullOrEmpty()) Uri.parse(currentUriStr) else null
        
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Tone")
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
        }
        
        ringtoneLauncher.launch(intent)
    }

    private fun saveSettings() {
        val intervalStr = binding.pollingIntervalEditText.text.toString()
        val interval = intervalStr.toIntOrNull() ?: 15
        
        if (interval < 1) {
            binding.pollingIntervalLayout.error = "Minimum 1 minute"
            return
        }
        
        binding.pollingIntervalLayout.error = null
        
        val enabled = binding.pollingEnabledSwitch.isChecked
        val alarm = binding.alarmEnabledSwitch.isChecked
        
        preferenceManager.setPollingEnabled(enabled)
        preferenceManager.setPollingInterval(interval)
        
        if (alarm && preferenceManager.getAlarmRingtoneUri().isNullOrEmpty()) {
            preferenceManager.initializeDefaultRingtone(requireContext())
            // Refresh the UI to show the auto-selected name
            val ringtoneName = preferenceManager.getAlarmRingtoneName() ?: "Default"
            binding.selectedRingtoneText.text = ringtoneName
        }
        
        preferenceManager.setAlarmEnabled(alarm)
        
        // Trigger WorkManager re-scheduling
        pollingManager.setupPollingWorker(force = true)
        
        android.widget.Toast.makeText(requireContext(), com.lifestyler.android.R.string.settings_saved_success, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 