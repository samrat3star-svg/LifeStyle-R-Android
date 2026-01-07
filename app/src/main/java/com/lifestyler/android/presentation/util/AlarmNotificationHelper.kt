package com.lifestyler.android.presentation.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lifestyler.android.R
import com.lifestyler.android.presentation.main.MainActivity

object AlarmNotificationHelper {

    private const val CHANNEL_ID_PREFIX = "alarm_channel_"
    private const val NOTIFICATION_ID = 999
    const val ACTION_STOP_ALARM = "com.lifestyler.android.ACTION_STOP_ALARM"

    fun triggerAlarm(context: Context, ringtoneUriString: String?, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Check permission if Tiramisu+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                android.util.Log.e("AlarmNotificationHelper", "POST_NOTIFICATIONS permission not granted!")
                return
            }
        }

        val uri = if (!ringtoneUriString.isNullOrEmpty()) Uri.parse(ringtoneUriString) else android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
        val channelId = "${CHANNEL_ID_PREFIX}${uri.hashCode()}"

        android.util.Log.d("AlarmNotificationHelper", "triggerAlarm: uri=$uri, channelId=$channelId")

        // Create Channel (Needed for O+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Fasting Alarm"
            val descriptionText = "Alarms for fasting updates"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setSound(uri, audioAttributes)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Open App Intent
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // Stop Action Intent (Ideally broadcast receiver)
        val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_sync) 
            .setContentTitle("Fasting Status Changed")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(contentPendingIntent, true) // Wakes the screen
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setSound(uri) // For pre-O
            .setVibrate(longArrayOf(0, 500, 500, 500))
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "STOP ALARM", stopPendingIntent)

        // Force Screen Wakeup using PowerManager
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            val wakeLock = pm.newWakeLock(
                android.os.PowerManager.FULL_WAKE_LOCK or 
                android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP or 
                android.os.PowerManager.ON_AFTER_RELEASE, 
                "FastingApp:AlarmWakeLock"
            )
            wakeLock.acquire(3000L) // Wake for 3 seconds
        } catch (e: Exception) {
            android.util.Log.e("AlarmNotificationHelper", "Failed to wake screen: ${e.message}")
        }

        val notification = builder.build()
        // CRITICAL: Triggers continuous ringing
        notification.flags = notification.flags or Notification.FLAG_INSISTENT
        
        android.util.Log.i("AlarmNotificationHelper", "Dispatching notification ID $NOTIFICATION_ID to system.")
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun stopAlarm(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
