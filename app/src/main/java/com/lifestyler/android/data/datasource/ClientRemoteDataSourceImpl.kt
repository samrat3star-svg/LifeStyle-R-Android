package com.lifestyler.android.data.datasource

import com.lifestyler.android.BuildConfig
import com.lifestyler.android.data.api.ApiService
import com.lifestyler.android.data.model.ClientsResponse
import com.lifestyler.android.data.model.ClientUpdateRequest
import com.lifestyler.android.data.model.ClientUpdateResponse
import com.lifestyler.android.data.model.PendingClientsResponse
import com.lifestyler.android.domain.entity.PendingClient
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRemoteDataSourceImpl @Inject constructor(
    private val apiService: ApiService
) : ClientRemoteDataSource {
    override suspend fun getPendingClients(): Response<PendingClientsResponse> {
        return apiService.getPendingClients()
    }
    
    override suspend fun getAllClients(): Response<ClientsResponse> {
        return apiService.getAllClients()
    }
    
    override suspend fun updateClient(request: ClientUpdateRequest): Response<ClientUpdateResponse> {
        return apiService.updateClient(request)
    }
} 