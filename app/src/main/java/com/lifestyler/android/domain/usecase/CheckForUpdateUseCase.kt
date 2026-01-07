package com.lifestyler.android.domain.usecase

import com.lifestyler.android.BuildConfig
import com.lifestyler.android.domain.entity.UpdateInfo
import com.lifestyler.android.domain.repository.UpdateRepository
import javax.inject.Inject

class CheckForUpdateUseCase @Inject constructor(
    private val repository: UpdateRepository
) {
    suspend operator fun invoke(): Result<UpdateInfo?> {
        val result = repository.getLatestUpdate()
        return result.map { updateInfo ->
            if (updateInfo != null && isNewerVersion(updateInfo.version)) {
                updateInfo
            } else {
                null
            }
        }
    }

    private fun isNewerVersion(remoteVersion: String): Boolean {
        val currentVersion = BuildConfig.VERSION_NAME
        // Strip 'v' if present (e.g. v1.0.0 -> 1.0.0)
        val remoteClean = remoteVersion.removePrefix("v").trim()
        val currentClean = currentVersion.removePrefix("v").trim()
        
        if (remoteClean == currentClean) return false
        
        // Simple comparison: if they are different, we assume remote is newer for this implementation
        // A more complex semver comparison could be used here if needed
        return remoteClean != currentClean
    }
}
