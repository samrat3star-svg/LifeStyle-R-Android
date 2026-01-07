package com.lifestyler.android.domain.usecase

import com.lifestyler.android.domain.entity.Client
import com.lifestyler.android.domain.repository.ClientRepository
import javax.inject.Inject

class CreateClientUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    
    suspend operator fun invoke(client: Client): Result<String> {
        return try {
            val clientId = clientRepository.createClient(client)
            Result.success(clientId)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
} 