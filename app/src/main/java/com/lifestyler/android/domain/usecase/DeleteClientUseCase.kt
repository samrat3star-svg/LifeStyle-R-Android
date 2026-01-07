package com.lifestyler.android.domain.usecase

import com.lifestyler.android.domain.repository.ClientRepository
import javax.inject.Inject

class DeleteClientUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    
    suspend operator fun invoke(clientId: String): Result<Boolean> {
        return try {
            val success = clientRepository.deleteClient(clientId)
            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete client"))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
} 