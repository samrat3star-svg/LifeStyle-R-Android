package com.lifestyler.android.data.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lifestyler.android.data.preference.PreferenceManager
import com.lifestyler.android.data.receiver.FastingPollingReceiver
import com.lifestyler.android.data.worker.FastingPollingWorker
import com.lifestyler.android.data.service.FastingForegroundService
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class PollingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: PreferenceManager
) {

    fun setupPollingWorker(force: Boolean = false) {
        val workManager = WorkManager.getInstance(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val tag = "PollingManager"
        
        val intent = Intent(context, FastingPollingReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (preferenceManager.isPollingEnabled()) {
            val interval = preferenceManager.getPollingInterval().toLong()
            android.util.Log.d(tag, "setupPollingWorker: Enabled, interval: $interval mins, force: $force")
            
            // Initialization/Rescue for ALL intervals
            var nextSync = preferenceManager.getNextSyncTime()
            val now = System.currentTimeMillis()
            val intervalMillis = TimeUnit.MINUTES.toMillis(interval)
            
            if (force || nextSync == 0L || nextSync <= now) {
                val oldSync = nextSync
                nextSync = now + intervalMillis
                preferenceManager.saveNextSyncTime(nextSync)
                android.util.Log.d(tag, "setupPollingWorker: Resetting timer! Force=$force, OldSync=$oldSync, NewSync=$nextSync, Gap=${nextSync - now}ms")
            }

            if (interval >= 15) {
                // 1. Standard Periodic Work
                android.util.Log.d(tag, "setupPollingWorker: Using WorkManager (interval >= 15)")
                FastingForegroundService.stopService(context) // Stop service if switching to long interval
                alarmManager.cancel(pendingIntent)
                
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                
                val pollingRequest = PeriodicWorkRequestBuilder<FastingPollingWorker>(
                    interval, TimeUnit.MINUTES,
                    5, TimeUnit.MINUTES
                )
                .setConstraints(constraints)
                .build()

                workManager.enqueueUniquePeriodicWork(
                    "FastingPollingWork",
                    if (force) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP,
                    pollingRequest
                )
            } else {
                // 2. Foreground Service for High-Frequency (1-14 mins)
                android.util.Log.d(tag, "setupPollingWorker: Using Foreground Service (interval < 15)")
                workManager.cancelUniqueWork("FastingPollingWork")
                alarmManager.cancel(pendingIntent) // Stop legacy receiver chain
                
                FastingForegroundService.startService(context)
            }
        } else {
            android.util.Log.d(tag, "setupPollingWorker: Polling disabled, cancelling all")
            workManager.cancelUniqueWork("FastingPollingWork")
            alarmManager.cancel(pendingIntent)
            FastingForegroundService.stopService(context)
        }
    }
}
