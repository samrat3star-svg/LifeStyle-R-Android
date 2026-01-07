package com.lifestyler.android.domain.usecase

import com.lifestyler.android.domain.entity.Client
import com.lifestyler.android.domain.repository.ClientRepository
import javax.inject.Inject

class UpdateClientUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    suspend operator fun invoke(id: String, client: Client): Result<Boolean> {
        return try {
            val success = clientRepository.updateClient(id, client)
            Result.success(success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 