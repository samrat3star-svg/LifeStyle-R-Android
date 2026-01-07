package com.lifestyler.android.data.datasource

import com.lifestyler.android.data.model.ClientsResponse
import com.lifestyler.android.data.model.ClientUpdateRequest
import com.lifestyler.android.data.model.ClientUpdateResponse
import com.lifestyler.android.data.model.PendingClientsResponse
import com.lifestyler.android.domain.entity.PendingClient
import retrofit2.Response

interface ClientRemoteDataSource {
    suspend fun getPendingClients(): Response<PendingClientsResponse>
    suspend fun getAllClients(): Response<ClientsResponse>
    suspend fun updateClient(request: ClientUpdateRequest): Response<ClientUpdateResponse>
} 