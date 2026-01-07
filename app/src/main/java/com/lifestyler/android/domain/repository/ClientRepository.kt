package com.lifestyler.android.domain.repository

import com.lifestyler.android.domain.entity.Client
import com.lifestyler.android.data.model.LoginResponse
import com.lifestyler.android.data.model.FastingSettingsResponse
import com.lifestyler.android.data.model.LogFastingResponse

interface ClientRepository {
    suspend fun getAllClients(): List<Client>
    suspend fun getPendingClients(): List<Client>
    suspend fun getClientById(clientId: String): Client?
    suspend fun createClient(client: Client): String
    suspend fun updateClient(clientId: String, client: Client): Boolean
    suspend fun deleteClient(clientId: String): Boolean
    suspend fun searchClientsByName(name: String): List<Client>
    suspend fun searchClientsByPhone(phone: String): List<Client>
    
    // Fasting Features
    suspend fun login(regCode: String, mobile: String): LoginResponse
    suspend fun getFastingSettings(sheetName: String): FastingSettingsResponse
    suspend fun logFasting(sheetName: String, duration: String): LogFastingResponse
    suspend fun followFasting(sheetName: String, fastingEndDate: String?): LogFastingResponse
} 