package com.lifestyler.android.presentation.fasting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifestyler.android.data.preference.PreferenceManager
import com.lifestyler.android.domain.usecase.GetFastingSettingsUseCase
import com.lifestyler.android.domain.usecase.LogFastingUseCase
import com.lifestyler.android.data.api.ApiService
import com.lifestyler.android.data.service.FastingForegroundService
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FastingDashboardViewModel @Inject constructor(
    private val getFastingSettingsUseCase: GetFastingSettingsUseCase,
    private val logFastingUseCase: LogFastingUseCase,
    private val preferenceManager: PreferenceManager,
    private val pollingManager: com.lifestyler.android.data.manager.PollingManager,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    data class DashboardState(
        val isLoading: Boolean = true,
        val userName: String = "User",
        val startTime: String = "--:--",
        val endTime: String = "--:--",
        val fastingHours: String = "--",
        val isFollowedToday: Boolean = false,
        val errorMessage: String? = null
    )

    data class PollingState(
        val isEnabled: Boolean = false,
        val nextSyncTime: String = "00:00",
        val lastSyncTime: String = "Never",
        val isVisible: Boolean = false,
        val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _pollingState = MutableStateFlow(PollingState())
    val pollingState: StateFlow<PollingState> = _pollingState.asStateFlow()
    
    // Track if a sync is currently in progress to prevent duplicate triggers
    private var isSyncing = false

    init {
        startSyncTimer()
    }

    private var tickCount = 0
    private fun startSyncTimer() {
        viewModelScope.launch {
            while (true) {
                try {
                    tickCount++
                    updatePollingStatus()
                } catch (e: Exception) {
                    android.util.Log.e("FastingDashboard", "CRASH in updatePollingStatus: ${e.message}", e)
                }
                delay(1000)
            }
        }
    }

    private fun updatePollingStatus() {
        val isEnabled = preferenceManager.isPollingEnabled()
        val lastSync = preferenceManager.getLastSyncTime()
        val lastError = preferenceManager.getLastSyncError()
        val nextSync = preferenceManager.getNextSyncTime()
        val now = System.currentTimeMillis()
        val diffMillis = nextSync - now
        
        // Diagnostic log every 10 ticks
        if (tickCount % 10 == 0) { 
            android.util.Log.d("FastingDashboard", "Tick $tickCount: now=$now, nextSync=$nextSync, diff=$diffMillis, isSyncing=$isSyncing, isEnabled=$isEnabled")
        }
        
        if (!isEnabled) {
            _pollingState.value = _pollingState.value.copy(isEnabled = false, isVisible = false)
            return
        }

        // AUTO-SYNC TRIGGER LOGIC
        if (nextSync == 0L && isEnabled) {
            // Initialization: Start the first timer immediately
            android.util.Log.d("FastingDashboard", "Initializing timer for $isEnabled")
            pollingManager.setupPollingWorker()
        }
        // NO LONGER CALLING loadSettings(sheetName) here! 
        // The Foreground Service handles the actual sync.
        // We just watch the PreferenceManager state change.

        val formattedLastSync = if (lastSync == 0L) "Never" else {
            val date = java.util.Date(lastSync)
            val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            sdf.format(date)
        }

        val countdown = if (_state.value.isLoading || isSyncing) {
            "Syncing..."
        } else if (nextSync == 0L) {
            "Initializing..."
        } else if (diffMillis <= 0) {
           if (lastError != null) "Retrying..." else "Checking..."
        } else {
            val totalSeconds = diffMillis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            String.format("%02d:%02d", minutes, seconds)
        }

        _pollingState.value = PollingState(
            isEnabled = true,
            nextSyncTime = countdown,
            lastSyncTime = formattedLastSync,
            isVisible = true,
            errorMessage = lastError
        )

        // SYNC UI DATA WITH PREFERENCES (Updated by Foreground Service)
        val pStart = preferenceManager.getLastStartTime()
        val pEnd = preferenceManager.getLastEndTime()
        val pHours = preferenceManager.getLastFastingHours()
        
        // We update the state if any persisted value differs from current UI state
        // or if we are still in the initial loading state.
        
        // CRITICAL: Do NOT update from prefs if we are manually loading (log/break).
        // This prevents the polling loop from clearing the "isLoading" flag prematurely.
        if (_state.value.isLoading) return

        if (pStart != null && (_state.value.startTime != pStart || _state.value.endTime != pEnd)) {
             _state.value = _state.value.copy(
                 userName = preferenceManager.getUserName() ?: "User",
                 startTime = pStart,
                 endTime = pEnd ?: "--:--",
                 fastingHours = pHours ?: "--",
                 isFollowedToday = preferenceManager.isLoggedToday(),
                 isLoading = false
             )
        }
    }

    fun loadSettings(sheetName: String) {
        viewModelScope.launch {
            loadSettingsInternal(sheetName)
        }
    }

    private suspend fun loadSettingsInternal(sheetName: String, lockoutMillis: Long = 0L) {
        if (isSyncing) {
            android.util.Log.d("FastingDashboard", "loadSettings: Already syncing, skipping.")
            return
        }
        try {
            android.util.Log.d("FastingDashboard", "loadSettings STARTED for $sheetName")
            isSyncing = true
            // Force Loading State (Lock UI)
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            
            getFastingSettingsUseCase(sheetName).onSuccess { response ->
                android.util.Log.d("FastingDashboard", "loadSettings SUCCESS: $response")
                preferenceManager.saveLastSyncTime(System.currentTimeMillis())
                preferenceManager.saveLastSyncError(null)
                
                val newStartTime = response.startTime ?: "--:--"
                val newEndTime = response.endTime ?: "--:--"
                val newFastingHours = response.fastingHours ?: "--"
                
                // Change tracking logic...
                val lastStartTime = preferenceManager.getLastStartTime()
                val lastEndTime = preferenceManager.getLastEndTime()
                val lastFastingHours = preferenceManager.getLastFastingHours()
                
                val isChanged = (lastStartTime != newStartTime) ||
                                (lastEndTime != newEndTime) ||
                                (lastFastingHours != newFastingHours)

                if (isChanged && preferenceManager.isAlarmEnabled()) {
                     val uri = preferenceManager.getAlarmRingtoneUri()
                     com.lifestyler.android.presentation.util.AlarmNotificationHelper.triggerAlarm(
                         context, 
                         uri,
                         "Fasting schedule updated: $newStartTime - $newEndTime ($newFastingHours hrs)"
                     )
                }
                
                // Update Persistence
                preferenceManager.saveLastStartTime(newStartTime)
                preferenceManager.saveLastEndTime(newEndTime)
                preferenceManager.saveLastFastingHours(newFastingHours)
                
                // MANDATORY LOCKOUT (if requested)
                if (lockoutMillis > 0) {
                    android.util.Log.d("FastingDashboard", "loadSettings: Entering lockout for ${lockoutMillis}ms")
                    delay(lockoutMillis)
                }

                android.util.Log.d("FastingDashboard", "loadSettings: SETTING isLoading=FALSE")
                _state.value = _state.value.copy(
                    isLoading = false,
                    userName = response.userName ?: "User",
                    startTime = newStartTime,
                    endTime = newEndTime,
                    fastingHours = newFastingHours,
                    isFollowedToday = response.isLoggedToday
                )
                
                // Force reset the background timer ONLY AFTER success
                pollingManager.setupPollingWorker(force = true)
            }.onFailure { e ->
                android.util.Log.e("FastingDashboard", "loadSettings FAILURE: ${e.message}")
                preferenceManager.saveLastSyncError(e.message)
                if (lockoutMillis > 0) delay(lockoutMillis) // Lockout even on failure
                _state.value = _state.value.copy(isLoading = false, errorMessage = e.message)
            }
        } catch (e: Exception) {
            android.util.Log.e("FastingDashboard", "loadSettings CRITICAL ERROR: ${e.message}", e)
            _state.value = _state.value.copy(isLoading = false, errorMessage = e.message)
        } finally {
            isSyncing = false
        }
    }

    fun manualSync() {
        val sheetName = preferenceManager.getSheetName()
        if (sheetName != null) {
            viewModelScope.launch {
                loadSettingsInternal(sheetName, lockoutMillis = 10000L)
            }
        }
    }

    fun logFasting(sheetName: String, duration: String) {
        if (_state.value.isLoading) return
        // SYNC UPDATE: Set loading immediately to block polling race condition
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            logFastingUseCase(sheetName, duration).onSuccess { response ->
                _state.value = _state.value.copy(
                    isFollowedToday = true
                )
                
                // Sync with 10s lockout
                loadSettingsInternal(sheetName, lockoutMillis = 10000L)
            }.onFailure { e ->
                _state.value = _state.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun breakFasting() {
        if (_state.value.isLoading) return
        val sheetName = preferenceManager.getSheetName() ?: return
        
        // SYNC UPDATE: Set loading immediately to block polling race condition
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    com.lifestyler.android.data.service.FastingServiceEntryPoint::class.java
                )
                val apiService = entryPoint.apiService()
                
                val sdf = java.text.SimpleDateFormat("dd/MMM/yyyy, HH:mm", java.util.Locale.ENGLISH)
                val deviceTime = sdf.format(java.util.Date())
                
                val response = apiService.breakFasting("breakfasting", sheetName, deviceTime)
                if (response.isSuccessful && response.body()?.success == true) {
                    // DEFER UPDATE: Do NOT set isFollowedToday = false here.
                    // Keep the current state (Goal Achieved) visible under the overlay.
                    // The sync below will update it when data is ready.
                    
                    // Sync with 10s lockout
                    loadSettingsInternal(sheetName, lockoutMillis = 10000L)
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false, 
                        errorMessage = response.body()?.message ?: "Failed to break fast"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}
