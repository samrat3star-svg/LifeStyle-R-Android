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
        
        val remoteClean = remoteVersion.removePrefix("v").trim()
        val currentClean = currentVersion.removePrefix("v").trim()
        
        if (remoteClean == currentClean) return false
        
        val remoteParts = remoteClean.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = currentClean.split(".").mapNotNull { it.toIntOrNull() }
        
        val maxLen = maxOf(remoteParts.size, currentParts.size)
        
        for (i in 0 until maxLen) {
            val remotePart = if (i < remoteParts.size) remoteParts[i] else 0
            val currentPart = if (i < currentParts.size) currentParts[i] else 0
            
            if (remotePart > currentPart) return true
            if (remotePart < currentPart) return false
        }
        
        // If we reach here, versions are numerically identical (e.g., 1.0 vs 1.0.0)
        // We should allow the update if the string representation is different,
        // as the developer likely wants to transition to the new format.
        // This will NOT loop because once updated, the strings will match exactly.
        return remoteClean != currentClean
    }
}
