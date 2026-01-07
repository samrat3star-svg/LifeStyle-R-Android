package com.lifestyler.android.domain.usecase

import com.lifestyler.android.domain.entity.Client
import com.lifestyler.android.domain.repository.ClientRepository
import javax.inject.Inject

class GetAllClientsUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    suspend operator fun invoke(): List<Client> {
        return clientRepository.getAllClients()
    }
} 