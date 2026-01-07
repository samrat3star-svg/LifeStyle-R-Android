package com.lifestyler.android.data.repository

import com.lifestyler.android.data.api.ApiService
import com.lifestyler.android.data.model.ClientUpdateRequest
import com.lifestyler.android.domain.entity.Client
import com.lifestyler.android.domain.repository.ClientRepository
import com.lifestyler.android.data.model.LoginRequest
import com.lifestyler.android.data.model.LogFastingRequest
import com.lifestyler.android.data.model.LoginResponse
import com.lifestyler.android.data.model.FastingSettingsResponse
import com.lifestyler.android.data.model.LogFastingResponse
import javax.inject.Inject

class NetworkClientRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ClientRepository {

    override suspend fun getAllClients(): List<Client> {
        val response = apiService.getAllClients()
        if (response.isSuccessful) {
            return response.body()?.data ?: emptyList()
        } else {
            throw Exception("Failed to fetch clients: ${response.message()}")
        }
    }

    override suspend fun getPendingClients(): List<Client> {
        val response = apiService.getPendingClients()
        if (response.isSuccessful) {
            return response.body()?.data?.map { pending ->
                // Map PendingClient to Client with default values
                Client(
                    id = "",
                    name = pending.name,
                    email = "",
                    phone = "",
                    age = 0,
                    gender = "",
                    weight = 0.0,
                    height = 0.0,
                    goal = "",
                    medicalConditions = "",
                    medications = "",
                    allergies = "",
                    emergencyContact = "",
                    emergencyPhone = "",
                    status = pending.status,
                    registrationDate = "",
                    notes = ""
                )
            } ?: emptyList()
        } else {
            throw Exception("Failed to fetch pending clients: ${response.message()}")
        }
    }

    override suspend fun updateClient(clientId: String, client: Client): Boolean {
        val request = ClientUpdateRequest(
            id = clientId,
            name = client.name,
            email = client.email,
            phone = client.phone
        )
        val response = apiService.updateClient(request)
        if (response.isSuccessful) {
            return response.body()?.success == true
        } else {
            throw Exception("Failed to update client: ${response.message()}")
        }
    }

    override suspend fun getClientById(clientId: String): Client? {
        // Not supported by API yet
        return null
    }

    override suspend fun createClient(client: Client): String {
        // Not supported by API yet
        return ""
    }

    override suspend fun deleteClient(clientId: String): Boolean {
        // Not supported by API yet
        return false
    }

    override suspend fun searchClientsByName(name: String): List<Client> {
        // Local filtering could be done, or API query if supported
        return emptyList()
    }

    override suspend fun searchClientsByPhone(phone: String): List<Client> {
         // Local filtering could be done, or API query if supported
        return emptyList()
    }

    override suspend fun login(regCode: String, mobile: String): LoginResponse {
        val response = apiService.login(action = "login", regCode = regCode, mobile = mobile)
        if (response.isSuccessful) {
            return response.body() ?: LoginResponse(false, null, null, "Empty response body", null)
        } else {
            return LoginResponse(false, null, null, "Login failed (${response.code()}): ${response.message()}", null)
        }
    }

    override suspend fun getFastingSettings(sheetName: String): FastingSettingsResponse {
        android.util.Log.d("NetworkRepo", "getFastingSettings calling API for $sheetName")
        val response = apiService.getClientSettings(action = "getClientSettings", sheetName = sheetName)
        if (response.isSuccessful) {
            val body = response.body()
            android.util.Log.d("NetworkRepo", "getFastingSettings SUCCESS: body=$body")
            return body ?: FastingSettingsResponse(false, null, null, null, null, false, "Empty response body")
        } else {
            android.util.Log.e("NetworkRepo", "getFastingSettings ERROR: code=${response.code()}, msg=${response.message()}")
            return FastingSettingsResponse(false, null, null, null, null, false, "Failed to fetch settings: ${response.message()}")
        }
    }

    override suspend fun logFasting(sheetName: String, duration: String): LogFastingResponse {
        val response = apiService.logFasting(action = "logFasting", sheetName = sheetName, duration = duration)
        if (response.isSuccessful) {
            return response.body() ?: LogFastingResponse(false, "Empty response body")
        } else {
            return LogFastingResponse(false, "Failed to log fasting: ${response.message()}")
        }
    }

    override suspend fun followFasting(sheetName: String, fastingEndDate: String?): LogFastingResponse {
        val response = apiService.followFasting(action = "followFasting", sheetName = sheetName, fastingEndDate = fastingEndDate)
        if (response.isSuccessful) {
            return response.body() ?: LogFastingResponse(false, "Empty response body")
        } else {
            return LogFastingResponse(false, "Failed to follow fasting: ${response.message()}")
        }
    }
}
