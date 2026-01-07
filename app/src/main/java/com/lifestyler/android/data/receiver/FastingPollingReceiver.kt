package com.lifestyler.android.data.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.*
import com.lifestyler.android.data.preference.PreferenceManager
import com.lifestyler.android.data.worker.FastingPollingWorker
import com.lifestyler.android.data.service.FastingForegroundService
import java.util.concurrent.TimeUnit

class FastingPollingReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val preferenceManager = PreferenceManager(context)
        val tag = "FastingPollingReceiver"
        android.util.Log.d(tag, "onReceive: Alarm fired")
        
        if (!preferenceManager.isPollingEnabled()) {
            android.util.Log.d(tag, "onReceive: Polling disabled, ignoring")
            return
        }

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            android.util.Log.d(tag, "onReceive: Boot completed, re-initializing")
            // After boot, re-initialize the alarm chain
            val interval = preferenceManager.getPollingInterval()
            if (interval < 15) {
                com.lifestyler.android.data.service.FastingForegroundService.startService(context)
            }
            return
        }

        // 1. Trigger the actual sync worker
        android.util.Log.d(tag, "onReceive: Enqueueing sync worker")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<FastingPollingWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "FastingPollingWork_Immediate",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )

        // 2. Schedule next alarm if interval < 15
        val interval = preferenceManager.getPollingInterval()
        if (interval < 15) {
            android.util.Log.d(tag, "onReceive: Scheduling next alarm for $interval mins")
            scheduleNextAlarm(context, interval)
        } else {
            android.util.Log.d(tag, "onReceive: Interval is $interval, letting WorkManager handle it")
        }
    }

    private fun scheduleNextAlarm(context: Context, intervalMins: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val preferenceManager = PreferenceManager(context)
        val tag = "FastingPollingReceiver"
        
        val intent = Intent(context, FastingPollingReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intervalMillis = TimeUnit.MINUTES.toMillis(intervalMins.toLong())
        val triggerTime = System.currentTimeMillis() + intervalMillis
        
        // Save for UI countdown
        preferenceManager.saveNextSyncTime(triggerTime)
        android.util.Log.d(tag, "scheduleNextAlarm: Next alarm set for $triggerTime")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
}
