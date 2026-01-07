package com.lifestyler.android.presentation.fasting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifestyler.android.data.preference.PreferenceManager
import com.lifestyler.android.domain.usecase.GetFastingSettingsUseCase
import com.lifestyler.android.domain.usecase.LogFastingUseCase
import com.lifestyler.android.data.api.ApiService
import com.lifestyler.android.data.service.FastingForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class FastingDashboardViewModel @Inject constructor(
    private val getFastingSettingsUseCase: GetFastingSettingsUseCase,
    private val logFastingUseCase: LogFastingUseCase,
    private val preferenceManager: PreferenceManager,
    private val pollingManager: com.lifestyler.android.data.manager.PollingManager,
    private val clientRepository: com.lifestyler.android.domain.repository.ClientRepository,
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

        val formattedLastSync = if (lastSync == 0L) "Never" else {
            val date = java.util.Date(lastSync)
            val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            sdf.format(date)
        }

        val isBackgroundSyncing = preferenceManager.isBackgroundSyncing()
        val countdown = if (_state.value.isLoading || isSyncing || isBackgroundSyncing) {
            "Syncing..."
        } else if (nextSync == 0L) {
            "Initializing..."
        } else if (diffMillis <= 0) {
           // WATCHDOG: If sync is overdue by >5 seconds, force a retry
           if (diffMillis < -5000 && isEnabled) {
               android.util.Log.w("FastingDashboard", "Sync overdue by ${-diffMillis}ms. Forcing retry.")
               pollingManager.setupPollingWorker(force = true)
           }
           if (lastError != null) "Retrying..." else "Syncing..."
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
        
        // CRITICAL: Do NOT update from prefs if we are manually loading (log/break).
        if (_state.value.isLoading) return

        val pIsLogged = preferenceManager.isLoggedToday()
        val pUser = preferenceManager.getUserName() ?: "User"
        
        val isDataChanged = pStart != null && (
            _state.value.startTime != pStart || 
            _state.value.endTime != pEnd || 
            _state.value.fastingHours != pHours ||
            _state.value.isFollowedToday != pIsLogged ||
            _state.value.userName != pUser
        )

        if (isDataChanged) {
             _state.value = _state.value.copy(
                 userName = pUser,
                 startTime = pStart!!,
                 endTime = pEnd ?: "--:--",
                 fastingHours = pHours ?: "--",
                 isFollowedToday = pIsLogged,
                 isLoading = false
             )
        }
    }

    fun loadSettings(sheetName: String, forceRefresh: Boolean = false) {
        // If not forcing refresh and we already have some data/syncing, don't trigger a new network call
        if (!forceRefresh && (_state.value.startTime != "--:--" || isSyncing)) {
            android.util.Log.d("FastingDashboard", "loadSettings: Using cached/existing state, skipping network call.")
            return
        }
        
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
            
            // Note: Cache is already cleared by the caller (manualSync, logFasting, breakFasting)
            
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
                    
                    // Concurrent pre-fetch for other screens while in lockout
                    viewModelScope.launch {
                        try {
                            val sheet = preferenceManager.getSheetName() ?: return@launch
                            android.util.Log.d("FastingDashboard", "Pre-fetching all data for $sheet")
                            kotlinx.coroutines.coroutineScope {
                                val measurements = async { clientRepository.getMeasurements(sheet) }
                                val breaks = async { clientRepository.getBreaks(sheet) }
                                measurements.await()
                                breaks.await()
                            }
                            android.util.Log.d("FastingDashboard", "Pre-fetch COMPLETED")
                        } catch (e: Exception) {
                            android.util.Log.e("FastingDashboard", "Pre-fetch FAILED: ${e.message}")
                        }
                    }
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
            clientRepository.clearCache()
            viewModelScope.launch {
                loadSettingsInternal(sheetName, lockoutMillis = 10000L)
            }
        }
    }

    fun logFasting(sheetName: String, duration: String) {
        if (_state.value.isLoading || isSyncing) return
        
        isSyncing = true
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        clientRepository.clearCache()
        
        viewModelScope.launch {
            try {
                logFastingUseCase(sheetName, duration).onSuccess { response ->
                    // User Request: Delay 3 seconds AFTER data arrives but BEFORE UI updates
                    delay(3000)

                    // ATOMIC UPDATE: Use the returned settings immediately
                    val newStartTime = response.startTime ?: "--:--"
                    val newEndTime = response.endTime ?: "--:--"
                    val newFastingHours = response.fastingHours ?: "--"
                    
                    // Update Persistence
                    preferenceManager.saveLastStartTime(newStartTime)
                    preferenceManager.saveLastEndTime(newEndTime)
                    preferenceManager.saveLastFastingHours(newFastingHours)

                    _state.value = _state.value.copy(
                        isLoading = false,
                        userName = response.userName ?: _state.value.userName,
                        startTime = newStartTime,
                        endTime = newEndTime,
                        fastingHours = newFastingHours,
                        isFollowedToday = response.isLoggedToday
                    )
                    
                    // Reset polling
                    pollingManager.setupPollingWorker(force = true)
                }.onFailure { e ->
                    // Even on failure, we might want to delay slightly or just show error
                    _state.value = _state.value.copy(isLoading = false, errorMessage = e.message)
                }
            } finally {
                isSyncing = false
            }
        }
    }

    fun breakFasting() {
        if (_state.value.isLoading || isSyncing) return
        
        isSyncing = true
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        clientRepository.clearCache()

        viewModelScope.launch {
            try {
                // Use Repository instead of direct API for cleaner architecture
                val sheetName = preferenceManager.getSheetName()
                if (sheetName == null) {
                    _state.value = _state.value.copy(isLoading = false, errorMessage = "Sheet name missing")
                    return@launch
                }

                val sdf = java.text.SimpleDateFormat("dd/MMM/yyyy, HH:mm", java.util.Locale.ENGLISH)
                val deviceTime = sdf.format(java.util.Date())
                
                val response = clientRepository.breakFasting(sheetName, deviceTime)
                
                if (response.success) {
                    // User Request: Delay 3 seconds AFTER data arrives but BEFORE UI updates
                    delay(3000)

                    // ATOMIC UPDATE: Use the returned settings immediately
                    val newStartTime = response.startTime ?: "--:--"
                    val newEndTime = response.endTime ?: "--:--"
                    val newFastingHours = response.fastingHours ?: "--"
                    
                    // Update Persistence
                    preferenceManager.saveLastStartTime(newStartTime)
                    preferenceManager.saveLastEndTime(newEndTime)
                    preferenceManager.saveLastFastingHours(newFastingHours)

                    _state.value = _state.value.copy(
                        isLoading = false,
                        userName = response.userName ?: _state.value.userName,
                        startTime = newStartTime,
                        endTime = newEndTime,
                        fastingHours = newFastingHours,
                        isFollowedToday = response.isLoggedToday
                    )
                    
                    // Reset polling
                    pollingManager.setupPollingWorker(force = true)
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false, 
                        errorMessage = response.message ?: "Failed to break fast"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = e.message)
            } finally {
                isSyncing = false
            }
        }
    }
}
