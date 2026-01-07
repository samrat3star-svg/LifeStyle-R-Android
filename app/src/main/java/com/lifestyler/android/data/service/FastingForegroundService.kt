package com.lifestyler.android.data.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.lifestyler.android.R
import com.lifestyler.android.data.api.ApiService
import com.lifestyler.android.data.model.FastingSettingsResponse
import com.lifestyler.android.data.preference.PreferenceManager
import com.lifestyler.android.presentation.main.MainActivity
import com.lifestyler.android.presentation.util.AlarmNotificationHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FastingServiceEntryPoint {
    fun apiService(): ApiService
    fun preferenceManager(): PreferenceManager
}

class FastingForegroundService : Service() {

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var apiService: ApiService
    private lateinit var wakeLock: android.os.PowerManager.WakeLock
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollingJob: Job? = null

    companion object {
        private const val CHANNEL_ID = "fasting_sync_service"
        private const val NOTIFICATION_ID = 888
        
        fun startService(context: Context) {
            val intent = Intent(context, FastingForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, FastingForegroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            FastingServiceEntryPoint::class.java
        )
        apiService = entryPoint.apiService()
        preferenceManager = entryPoint.preferenceManager()
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Syncing fasting status..."))
        
        // Acquire Wakelock to keep CPU running during standby
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "FastingApp:PollingWakeLock")
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes fallback*/)
        
        startPolling()
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = serviceScope.launch {
            while (isActive) {
                try {
                    val isEnabled = preferenceManager.isPollingEnabled()
                    val intervalMinutes = preferenceManager.getPollingInterval().toLong()
                    
                    if (isEnabled && intervalMinutes < 15) {
                        // 1. Immediately schedule the NEXT sync for the UI timer
                        val nextSync = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(intervalMinutes)
                        preferenceManager.saveNextSyncTime(nextSync)
                        
                        val sheetName = preferenceManager.getSheetName()
                        if (sheetName != null) {
                            android.util.Log.i("FastingService", ">>> Sync START for $sheetName")
                            val response = apiService.getClientSettings("getClientSettings", sheetName)
                            if (response.isSuccessful) {
                                val settings = response.body()
                                android.util.Log.d("FastingService", ">>> Sync SUCCESS: ${settings?.success}")
                                if (settings?.success == true) {
                                    handleSettingsUpdate(settings)
                                }
                            } else {
                                android.util.Log.e("FastingService", ">>> Sync FAILED with code: ${response.code()}")
                            }
                        }
                    } else if (!isEnabled || intervalMinutes >= 15) {
                        android.util.Log.d("FastingService", "Service stopping: isEnabled=$isEnabled, interval=$intervalMinutes")
                        stopSelf()
                        break
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FastingService", "Polling error: ${e.message}")
                }
                
                delay(TimeUnit.MINUTES.toMillis(preferenceManager.getPollingInterval().toLong()))
            }
        }
    }

    private fun handleSettingsUpdate(settings: FastingSettingsResponse) {
        val newStartTime = settings.startTime ?: "--:--"
        val newEndTime = settings.endTime ?: "--:--"
        val newFastingHours = settings.fastingHours ?: "--"
        
        val lastStartTime = preferenceManager.getLastStartTime()
        val lastEndTime = preferenceManager.getLastEndTime()
        val lastFastingHours = preferenceManager.getLastFastingHours()

        val isChanged = (lastStartTime != newStartTime) ||
                        (lastEndTime != newEndTime) ||
                        (lastFastingHours != newFastingHours)

        if (isChanged && preferenceManager.isAlarmEnabled()) {
             val uri = preferenceManager.getAlarmRingtoneUri()
             AlarmNotificationHelper.triggerAlarm(
                 applicationContext, 
                 uri,
                 "Fasting schedule updated: $newStartTime - $newEndTime ($newFastingHours hrs)"
             )
        }
        
        // Update state
        preferenceManager.saveLastStartTime(newStartTime)
        preferenceManager.saveLastEndTime(newEndTime)
        preferenceManager.saveLastFastingHours(newFastingHours)
        preferenceManager.saveUserName(settings.userName ?: "User")
        preferenceManager.setLoggedToday(settings.isLoggedToday)
        preferenceManager.saveLastSyncTime(System.currentTimeMillis())
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification("Active: $newStartTime - $newEndTime"))
    }

    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("LifeStyle-R Sync Active")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_sync)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSilent(true)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fasting Sync Service",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Keeps fasting sync active in background"
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        if (wakeLock.isHeld) wakeLock.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
