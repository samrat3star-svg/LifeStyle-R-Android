package com.lifestyler.android.data.preference

import android.content.Context
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_REG_CODE = "reg_code"
        private const val KEY_MOBILE = "mobile"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_SHEET_NAME = "sheet_name"
        private const val KEY_POLLING_ENABLED = "polling_enabled"
        private const val KEY_POLLING_INTERVAL = "polling_interval"
        private const val KEY_ALARM_ENABLED = "alarm_enabled"
        private const val KEY_ALARM_RINGTONE_URI = "alarm_ringtone_uri"
        private const val KEY_ALARM_RINGTONE_NAME = "alarm_ringtone_name"
        private const val KEY_LAST_KNOWN_START_TIME = "last_known_start_time"
        private const val KEY_LAST_KNOWN_END_TIME = "last_known_end_time"
        private const val KEY_LAST_KNOWN_FASTING_HOURS = "last_known_fasting_hours"
        private const val KEY_LAST_KNOWN_USER_NAME = "last_known_user_name"
        private const val KEY_LAST_KNOWN_LOGGED_TODAY = "last_known_logged_today"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_NEXT_SYNC_TIME = "next_sync_time"
        private const val KEY_LAST_SYNC_ERROR = "last_sync_error"
        private const val KEY_IS_BACKGROUND_SYNCING = "is_background_syncing"
        private const val KEY_UPDATE_IS_DOWNLOADING = "update_is_downloading"
        private const val KEY_LAST_AUTO_INSTALL_ATTEMPTED_VERSION = "last_auto_install_attempted_version"
    }

    fun setLastAutoInstallAttemptedVersion(version: String) {
        prefs.edit().putString(KEY_LAST_AUTO_INSTALL_ATTEMPTED_VERSION, version).apply()
    }

    fun getLastAutoInstallAttemptedVersion(): String? = prefs.getString(KEY_LAST_AUTO_INSTALL_ATTEMPTED_VERSION, null)

    fun setUpdateDownloading(isDownloading: Boolean) {
        prefs.edit().putBoolean(KEY_UPDATE_IS_DOWNLOADING, isDownloading).apply()
    }

    fun isUpdateDownloading(): Boolean = prefs.getBoolean(KEY_UPDATE_IS_DOWNLOADING, false)

    fun setIsBackgroundSyncing(isSyncing: Boolean) {
        prefs.edit().putBoolean(KEY_IS_BACKGROUND_SYNCING, isSyncing).apply()
    }

    fun isBackgroundSyncing(): Boolean = prefs.getBoolean(KEY_IS_BACKGROUND_SYNCING, false)

    fun saveSheetName(sheetName: String) {
        prefs.edit().putString(KEY_SHEET_NAME, sheetName).apply()
    }

    fun getSheetName(): String? = prefs.getString(KEY_SHEET_NAME, null)

    fun setPollingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_POLLING_ENABLED, enabled).apply()
    }

    fun isPollingEnabled(): Boolean = prefs.getBoolean(KEY_POLLING_ENABLED, true)

    fun setPollingInterval(minutes: Int) {
        prefs.edit().putInt(KEY_POLLING_INTERVAL, minutes).apply()
    }

    fun getPollingInterval(): Int = prefs.getInt(KEY_POLLING_INTERVAL, 15)

    fun setAlarmEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALARM_ENABLED, enabled).apply()
    }

    fun isAlarmEnabled(): Boolean = prefs.getBoolean(KEY_ALARM_ENABLED, true)

    fun setAlarmRingtoneUri(uri: String) {
        prefs.edit().putString(KEY_ALARM_RINGTONE_URI, uri).apply()
    }

    fun getAlarmRingtoneUri(): String? {
        val uri = prefs.getString(KEY_ALARM_RINGTONE_URI, null)
        return if (uri.isNullOrEmpty()) null else uri
    }

    fun initializeDefaultRingtone(context: Context) {
        if (!getAlarmRingtoneUri().isNullOrEmpty()) return

        val manager = RingtoneManager(context)
        manager.setType(RingtoneManager.TYPE_NOTIFICATION)
        val cursor = manager.cursor
        var bellUri: Uri? = null
        var bellName = "Default"

        try {
            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                if (title.contains("Bell", ignoreCase = true)) {
                    bellUri = manager.getRingtoneUri(cursor.position)
                    bellName = title
                    break
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferenceManager", "Error searching for Bell ringtone: ${e.message}")
        } finally {
            // Cursor is managed by RingtoneManager, but we should be careful
        }

        if (bellUri != null) {
            setAlarmRingtoneUri(bellUri.toString())
            setAlarmRingtoneName(bellName)
        } else {
            // Fallback to default system notification sound
            val defaultUri = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
            setAlarmRingtoneUri(defaultUri.toString())
            setAlarmRingtoneName("Default")
        }
    }

    fun setAlarmRingtoneName(name: String) {
        prefs.edit().putString(KEY_ALARM_RINGTONE_NAME, name).apply()
    }

    fun getAlarmRingtoneName(): String? = prefs.getString(KEY_ALARM_RINGTONE_NAME, null)

    fun saveLastStartTime(startTime: String) {
        prefs.edit().putString(KEY_LAST_KNOWN_START_TIME, startTime).apply()
    }

    fun getLastStartTime(): String? = prefs.getString(KEY_LAST_KNOWN_START_TIME, null)

    fun saveLastEndTime(endTime: String) {
        prefs.edit().putString(KEY_LAST_KNOWN_END_TIME, endTime).apply()
    }

    fun getLastEndTime(): String? = prefs.getString(KEY_LAST_KNOWN_END_TIME, null)

    fun saveLastFastingHours(hours: String) {
        prefs.edit().putString(KEY_LAST_KNOWN_FASTING_HOURS, hours).apply()
    }

    fun getLastFastingHours(): String? = prefs.getString(KEY_LAST_KNOWN_FASTING_HOURS, null)

    fun saveUserName(name: String) {
        prefs.edit().putString(KEY_LAST_KNOWN_USER_NAME, name).apply()
    }

    fun getUserName(): String? = prefs.getString(KEY_LAST_KNOWN_USER_NAME, null)

    fun setLoggedToday(logged: Boolean) {
        prefs.edit().putBoolean(KEY_LAST_KNOWN_LOGGED_TODAY, logged).apply()
    }

    fun isLoggedToday(): Boolean = prefs.getBoolean(KEY_LAST_KNOWN_LOGGED_TODAY, false)

    fun saveLastSyncTime(timeMillis: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC_TIME, timeMillis).apply()
    }

    fun getLastSyncTime(): Long = prefs.getLong(KEY_LAST_SYNC_TIME, 0L)

    fun saveNextSyncTime(timeMillis: Long) {
        prefs.edit().putLong(KEY_NEXT_SYNC_TIME, timeMillis).apply()
    }

    fun getNextSyncTime(): Long = prefs.getLong(KEY_NEXT_SYNC_TIME, 0L)

    fun saveLastSyncError(error: String?) {
        prefs.edit().putString(KEY_LAST_SYNC_ERROR, error).apply()
    }

    fun getLastSyncError(): String? = prefs.getString(KEY_LAST_SYNC_ERROR, null)

    fun saveCredentials(regCode: String, mobile: String) {
        prefs.edit().apply {
            putString(KEY_REG_CODE, regCode)
            putString(KEY_MOBILE, mobile)
            putBoolean(KEY_REMEMBER_ME, true)
            apply()
        }
    }

    fun clearCredentials() {
        prefs.edit().apply {
            remove(KEY_REG_CODE)
            remove(KEY_MOBILE)
            putBoolean(KEY_REMEMBER_ME, false)
            apply()
        }
    }

    fun getRegCode(): String? = prefs.getString(KEY_REG_CODE, null)
    fun getMobile(): String? = prefs.getString(KEY_MOBILE, null)
    fun isRememberMeChecked(): Boolean = prefs.getBoolean(KEY_REMEMBER_ME, false)
}
