package com.lifestyler.android.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.lifestyler.android.data.api.ApiService
import com.lifestyler.android.data.preference.PreferenceManager
import com.lifestyler.android.util.NotificationHelper
import com.lifestyler.android.presentation.util.AlarmNotificationHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FastingPollingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FastingWorkerEntryPoint {
        fun apiService(): ApiService
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val preferenceManager = PreferenceManager(applicationContext)
        val tag = "FastingPollingWorker"
        android.util.Log.d(tag, "doWork: Polling started")
        
        // 1. Check if polling is enabled
        if (!preferenceManager.isPollingEnabled()) {
            android.util.Log.d(tag, "doWork: Polling disabled, exiting")
            return@withContext Result.success()
        }

        val sheetName = preferenceManager.getSheetName()
        if (sheetName == null) {
            android.util.Log.e(tag, "doWork: Sheet name is null, failing")
            return@withContext Result.failure()
        }
        
        // 2. Access ApiService via EntryPoint ...
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            FastingWorkerEntryPoint::class.java
        )
        val apiService = entryPoint.apiService()

        try {
            android.util.Log.d(tag, "doWork: Fetching latest schedule for $sheetName")
            // 3. Fetch latest schedule
            val response = apiService.getClientSettings("getClientSettings", sheetName)
            if (response.isSuccessful) {
                android.util.Log.d(tag, "doWork: Sync successful")
                // Success: clear error and update sync time
                preferenceManager.saveLastSyncError(null)
                preferenceManager.saveLastSyncTime(System.currentTimeMillis())
                
                // For intervals >= 15, WorkManager handles it, but we update nextSyncTime for UI estimate
                val interval = preferenceManager.getPollingInterval()
                if (interval >= 15) {
                    val nextSync = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(interval.toLong())
                    preferenceManager.saveNextSyncTime(nextSync)
                    android.util.Log.d(tag, "doWork: Updated nextSyncTime for PeriodicWork: $nextSync")
                }
                
                val settings = response.body()
                if (settings?.success == true) {
                    val newStartTime = settings.startTime ?: "--:--"
                    val newEndTime = settings.endTime ?: "--:--"
                    val newFastingHours = settings.fastingHours ?: "--"
                    
                    val lastStartTime = preferenceManager.getLastStartTime()
                    val lastEndTime = preferenceManager.getLastEndTime()
                    val lastFastingHours = preferenceManager.getLastFastingHours()

                    // 4. Compare and Trigger Alarm
                    val isChanged = (lastStartTime != newStartTime) ||
                                    (lastEndTime != newEndTime) ||
                                    (lastFastingHours != newFastingHours)

                    android.util.Log.d(tag, "doWork Check: last=[$lastStartTime, $lastEndTime, $lastFastingHours], new=[$newStartTime, $newEndTime, $newFastingHours], isChanged=$isChanged")

                    if (isChanged) {
                        android.util.Log.i(tag, "doWork: Data CHANGE detected! Triggering Alarm. AlarmEnabled=${preferenceManager.isAlarmEnabled()}")
                        if (preferenceManager.isAlarmEnabled()) {
                            val uri = preferenceManager.getAlarmRingtoneUri()
                            AlarmNotificationHelper.triggerAlarm(
                                applicationContext, 
                                uri,
                                "Fasting schedule updated: $newStartTime - $newEndTime ($newFastingHours hrs)"
                            )
                        }
                    }
                    
                    // Always update the last known state
                    preferenceManager.saveLastStartTime(newStartTime)
                    preferenceManager.saveLastEndTime(newEndTime)
                    preferenceManager.saveLastFastingHours(newFastingHours)
                }
            } else {
                val errorMsg = "Server Error: ${response.code()}"
                android.util.Log.e(tag, "doWork: $errorMsg")
                preferenceManager.saveLastSyncError(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Network Error: ${e.message}"
            android.util.Log.e(tag, "doWork: $errorMsg")
            preferenceManager.saveLastSyncError(errorMsg)
        }

        Result.success()
    }
}
