package com.lifestyler.android.data.receiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.lifestyler.android.BuildConfig
import java.io.File
import android.os.Environment

class UpdateDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadId == -1L) return

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val preferenceManager = com.lifestyler.android.data.preference.PreferenceManager(context)
            
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)

            try {
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    val title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))

                    android.util.Log.d("UpdateReceiver", "Download $downloadId finished with status $status, title: $title")

                    // Only handle our app updates
                    if (title.contains("LifeStyle-R Update")) {
                        // CRITICAL: Always reset the flag so user can try again if it failed
                        preferenceManager.setUpdateDownloading(false)

                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            installApk(context, downloadId)
                        } else {
                            val reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                            android.util.Log.e("UpdateReceiver", "Download FAILED with reason: $reason")
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("UpdateReceiver", "Error processing download: ${e.message}")
            } finally {
                cursor.close()
            }
        }
    }

    private fun installApk(context: Context, downloadId: Long) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            var filePath: String? = null
            if (cursor.moveToFirst()) {
                val localUriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                if (localUriString != null) {
                    filePath = Uri.parse(localUriString).path
                }
            }
            cursor.close()

            if (filePath != null) {
                val file = File(filePath)
                if (file.exists()) {
                    android.util.Log.i("UpdateReceiver", "Generating FileProvider URI for: ${file.absolutePath}")
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )

                    val installIntent = Intent(Intent.ACTION_VIEW).apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        setDataAndType(contentUri, "application/vnd.android.package-archive")
                    }

                    // Fallback: Always show a notification so user can install if the auto-popup is blocked
                    showInstallNotification(context, installIntent)

                    try {
                        context.startActivity(installIntent)
                        android.util.Log.i("UpdateReceiver", "Auto-install intent sent successfully.")
                    } catch (e: Exception) {
                        android.util.Log.e("UpdateReceiver", "Auto-install intent blocked (likely background): ${e.message}")
                    }
                    return
                }
            }
            android.util.Log.e("UpdateReceiver", "Failed to resolve physical file for downloadId: $downloadId")
        } catch (e: Exception) {
            android.util.Log.e("UpdateReceiver", "Auto-install logic failed: ${e.message}")
        }
    }

    private fun showInstallNotification(context: Context, installIntent: Intent) {
        val channelId = "update_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "App Updates",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for app updates"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            installIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.lifestyler.android.R.drawable.ic_sync) // Reuse an existing icon
            .setContentTitle("Update Ready to Install")
            .setContentText("The new version of LifeStyle-R is ready. Tap to install now.")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true) // Aggressive pop-up attempt
            .build()

        notificationManager.notify(1001, notification)
    }
}
