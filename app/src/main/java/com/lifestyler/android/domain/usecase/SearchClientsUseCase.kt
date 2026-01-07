package com.lifestyler.android.domain.usecase

import com.lifestyler.android.domain.entity.Client
import com.lifestyler.android.domain.repository.ClientRepository
import javax.inject.Inject

class SearchClientsUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    
    suspend fun searchByName(name: String): Result<List<Client>> {
        return try {
            val clients = clientRepository.searchClientsByName(name)
            Result.success(clients)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
    
    suspend fun searchByPhone(phone: String): Result<List<Client>> {
        return try {
            val clients = clientRepository.searchClientsByPhone(phone)
            Result.success(clients)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
} 