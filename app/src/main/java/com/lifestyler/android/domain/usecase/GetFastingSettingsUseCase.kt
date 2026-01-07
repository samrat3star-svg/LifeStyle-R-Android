package com.lifestyler.android.domain.usecase

import com.lifestyler.android.data.model.FastingSettingsResponse
import com.lifestyler.android.domain.repository.ClientRepository
import javax.inject.Inject

class GetFastingSettingsUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    suspend operator fun invoke(sheetName: String): Result<FastingSettingsResponse> {
        return try {
            val result = clientRepository.getFastingSettings(sheetName)
            if (result.success) {
                Result.success(result)
            } else {
                Result.failure(Exception(result.message ?: "Failed to fetch settings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
