package com.lifestyler.android.presentation.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AlarmNotificationHelper.ACTION_STOP_ALARM) {
            AlarmNotificationHelper.stopAlarm(context)
        }
    }
}
