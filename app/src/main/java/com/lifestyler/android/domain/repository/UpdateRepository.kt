package com.lifestyler.android.domain.repository

import com.lifestyler.android.domain.entity.UpdateInfo

interface UpdateRepository {
    suspend fun getLatestUpdate(): Result<UpdateInfo?>
}
