package com.lifestyler.android.domain.usecase

import com.lifestyler.android.domain.entity.Client
import com.lifestyler.android.domain.repository.ClientRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetPendingClientsUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    suspend operator fun invoke(): List<Client> {
        return withContext(Dispatchers.IO) {
            clientRepository.getPendingClients()
        }
    }
} 