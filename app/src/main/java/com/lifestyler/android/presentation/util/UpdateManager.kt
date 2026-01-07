package com.lifestyler.android.presentation.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.lifestyler.android.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Singleton
class UpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: com.lifestyler.android.data.preference.PreferenceManager
) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private var downloadId: Long = -1

    fun downloadAndInstall(url: String, fileName: String): Boolean {
        // 1. Check if we think we're already downloading
        if (preferenceManager.isUpdateDownloading()) {
            if (isDownloadActuallyRunning()) {
                android.util.Log.w("UpdateManager", "Download already active in system, blocking new request.")
                return false
            } else {
                android.util.Log.i("UpdateManager", "Stale download flag detected, clearing.")
                preferenceManager.setUpdateDownloading(false)
            }
        }
        
        if (isDownloadActuallyRunning()) {
            android.util.Log.w("UpdateManager", "System DM is busy with LifeStyle-R Update, blocking.")
            preferenceManager.setUpdateDownloading(true)
            return false
        }

        // 2. IMPORTANT: Clean up ALL previous records and files for this update
        removeOldDownloadRecords(fileName)

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("LifeStyle-R Update")
            .setDescription("Downloading latest version...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setMimeType("application/vnd.android.package-archive")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        downloadId = downloadManager.enqueue(request)
        preferenceManager.setUpdateDownloading(true)
        android.util.Log.i("UpdateManager", "Download enqueued: $downloadId")
        return true
    }

    private fun removeOldDownloadRecords(fileName: String) {
        try {
            // Remove from DownloadManager database
            val query = DownloadManager.Query() // All records
            val cursor = downloadManager.query(query)
            try {
                while (cursor.moveToNext()) {
                    val title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
                    val localUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    if (title == "LifeStyle-R Update" && localUri != null && localUri.contains(fileName)) {
                        val id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID))
                        android.util.Log.i("UpdateManager", "Removing old DM record: $id")
                        downloadManager.remove(id)
                    }
                }
            } finally {
                cursor.close()
            }

            // Remove physical file
            val existingFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            if (existingFile.exists()) {
                android.util.Log.i("UpdateManager", "Deleting old file: ${existingFile.absolutePath}")
                existingFile.delete()
            }
        } catch (e: Exception) {
            android.util.Log.e("UpdateManager", "Cleanup failed: ${e.message}")
        }
    }

    fun installIfAlreadyDownloaded(fileName: String, version: String, autoTrigger: Boolean = false): Boolean {
        // Guard: If auto-triggering on launch, don't do it if we already tried this exact version
        if (autoTrigger && preferenceManager.getLastAutoInstallAttemptedVersion() == version) {
            android.util.Log.i("UpdateManager", "Skip auto-install on launch: already attempted for version $version")
            return false
        }

        val downloadId = getSuccessfulDownloadId(fileName)
        if (downloadId != -1L) {
            if (autoTrigger) {
                android.util.Log.i("UpdateManager", "Auto-triggering install for $fileName")
                preferenceManager.setLastAutoInstallAttemptedVersion(version)
            }
            installApk(downloadId)
            return true
        }
        return false
    }

    fun getSuccessfulDownloadId(fileName: String): Long {
        val query = DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)
        val cursor = downloadManager.query(query)
        var foundId: Long = -1
        try {
            while (cursor.moveToNext()) {
                val title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
                val localUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                if (title == "LifeStyle-R Update" && localUri != null && localUri.contains(fileName)) {
                    val id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID))
                    
                    val fileUri = downloadManager.getUriForDownloadedFile(id)
                    if (fileUri != null) {
                        val fileSize = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        // Increased minimum size to 2MB to be safer, most APKs are much larger
                        if (fileSize > 2 * 1024 * 1024) { 
                            foundId = id
                            break
                        } else {
                            android.util.Log.w("UpdateManager", "File suspicious: $fileSize bytes. Removing record.")
                            downloadManager.remove(id)
                        }
                    } else {
                        downloadManager.remove(id)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("UpdateManager", "Query failed: ${e.message}")
        } finally {
            cursor.close()
        }
        return foundId
    }

    fun installApk(downloadId: Long) {
        try {
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
            
            val file = if (filePath != null) File(filePath) else null
            
            if (file != null && file.exists()) {
                android.util.Log.i("UpdateManager", "Installing fromResolvedPath: ${file.absolutePath}")
                triggerInstall(file)
            } else {
                android.util.Log.e("UpdateManager", "File path invalid or missing. Attempting fallback by title...")
                // Last ditch: try to find the file manually in Downloads if we can't get the path from DM
                // (Though getSuccessfulDownloadId already does some title matching)
            }
        } catch (e: Exception) {
            android.util.Log.e("UpdateManager", "Install failed: ${e.message}")
        }
    }

    private fun triggerInstall(file: File) {
        try {
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
            context.startActivity(installIntent)
        } catch (e: Exception) {
            android.util.Log.e("UpdateManager", "triggerInstall failed: ${e.message}")
        }
    }

    fun openInBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("UpdateManager", "Browser fallback failed: ${e.message}")
        }
    }

    private fun isDownloadActuallyRunning(): Boolean {
        val query = DownloadManager.Query().setFilterByStatus(
            DownloadManager.STATUS_RUNNING or 
            DownloadManager.STATUS_PENDING or 
            DownloadManager.STATUS_PAUSED
        )
        val cursor = downloadManager.query(query)
        var running = false
        try {
            while (cursor.moveToNext()) {
                val title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
                if (title == "LifeStyle-R Update") {
                    running = true
                    break
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("UpdateManager", "Query failed: ${e.message}")
        } finally {
            cursor.close()
        }
        return running
    }

    // Reset flag and cleanup redundant files
    fun setNotDownloading() {
        preferenceManager.setUpdateDownloading(false)
        smartCleanup(BuildConfig.VERSION_NAME)
    }

    fun smartCleanup(installedVersion: String) {
        try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val files = downloadDir.listFiles()
            files?.forEach { file ->
                if (file.name.startsWith("LifeStyle-R-") && file.name.endsWith(".apk")) {
                    val fileVersion = extractVersion(file.name)
                    if (fileVersion != null) {
                        // If file version is NOT newer than installed version, it's redundant.
                        // We use the same comparison logic as CheckForUpdateUseCase if possible,
                        // but for cleanup, a simple string compare or numeric check is usually enough
                        // since we only care about older ones.
                        if (!isVersionNewer(fileVersion, installedVersion)) {
                            android.util.Log.i("UpdateManager", "Deleting redundant APK: ${file.name} (Installed: $installedVersion)")
                            file.delete()
                            // Also remove from DownloadManager to keep the UI clean
                            removeRecordByUri(file.absolutePath)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("UpdateManager", "Smart cleanup failed: ${e.message}")
        }
    }

    private fun removeRecordByUri(absolutePath: String) {
        try {
            val query = DownloadManager.Query()
            val cursor = downloadManager.query(query)
            try {
                while (cursor.moveToNext()) {
                    val localUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    if (localUri != null && localUri.contains(absolutePath)) {
                        val id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID))
                        downloadManager.remove(id)
                    }
                }
            } finally {
                cursor.close()
            }
        } catch (e: Exception) { /* ignore */ }
    }

    private fun extractVersion(fileName: String): String? {
        // Expected format: LifeStyle-R-1.0.0.apk
        return try {
            fileName.substringAfter("LifeStyle-R-").substringBefore(".apk")
        } catch (e: Exception) {
            null
        }
    }

    private fun isVersionNewer(newVersion: String, currentVersion: String): Boolean {
        val v1Parts = newVersion.split(".").mapNotNull { it.toIntOrNull() }
        val v2Parts = currentVersion.split(".").mapNotNull { it.toIntOrNull() }
        
        val length = maxOf(v1Parts.size, v2Parts.size)
        for (i in 0 until length) {
            val v1 = v1Parts.getOrElse(i) { 0 }
            val v2 = v2Parts.getOrElse(i) { 0 }
            if (v1 > v2) return true
            if (v1 < v2) return false
        }
        return false
    }
}
