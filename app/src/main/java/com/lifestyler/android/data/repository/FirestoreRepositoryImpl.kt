package com.lifestyler.android.data.repository

import com.lifestyler.android.data.datasource.FirestoreDataSource
import com.lifestyler.android.data.model.LoginResponse
import com.lifestyler.android.data.model.FastingSettingsResponse
import com.lifestyler.android.data.model.LogFastingResponse
import com.lifestyler.android.domain.entity.Client
import com.lifestyler.android.domain.repository.ClientRepository
import javax.inject.Inject

class FirestoreRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource
) : ClientRepository {
    
    // ClientRepository Implementation
    override suspend fun getAllClients(): List<Client> {
        return firestoreDataSource.getAllClients()
    }
    
    override suspend fun getPendingClients(): List<Client> {
        return firestoreDataSource.getPendingClients()
    }
    
    override suspend fun getClientById(clientId: String): Client? {
        return firestoreDataSource.getClientById(clientId)
    }
    
    override suspend fun createClient(client: Client): String {
        return firestoreDataSource.createClient(client)
    }
    
    override suspend fun updateClient(clientId: String, client: Client): Boolean {
        return firestoreDataSource.updateClient(clientId, client)
    }
    
    override suspend fun deleteClient(clientId: String): Boolean {
        return firestoreDataSource.deleteClient(clientId)
    }
    
    override suspend fun searchClientsByName(name: String): List<Client> {
        return firestoreDataSource.searchClientsByName(name)
    }
    
    override suspend fun searchClientsByPhone(phone: String): List<Client> {
        return firestoreDataSource.searchClientsByPhone(phone)
    }
    
    // Fasting Features - ClientRepository
    override suspend fun login(regCode: String, mobile: String): LoginResponse {
        // Not implemented for Firestore - should use NetworkClientRepositoryImpl
        return LoginResponse(
            success = false,
            sheetName = null,
            userName = null,
            message = "Firestore repository does not support login. Use NetworkClientRepositoryImpl.",
            token = null
        )
    }
    
    override suspend fun getFastingSettings(sheetName: String): FastingSettingsResponse {
        // Not implemented for Firestore - should use NetworkClientRepositoryImpl
        return FastingSettingsResponse(
            success = false,
            fastingHours = null,
            startTime = null,
            endTime = null,
            userName = null,
            isLoggedToday = false,
            message = "Firestore repository does not support fasting settings. Use NetworkClientRepositoryImpl."
        )
    }
    
    override suspend fun logFasting(sheetName: String, duration: String): LogFastingResponse {
        // Not implemented for Firestore - should use NetworkClientRepositoryImpl
        return LogFastingResponse(
            success = false,
            message = "Firestore repository does not support log fasting. Use NetworkClientRepositoryImpl."
        )
    }

    override suspend fun followFasting(sheetName: String, fastingEndDate: String?): LogFastingResponse {
        // Not implemented for Firestore - should use NetworkClientRepositoryImpl
        return LogFastingResponse(
            success = false,
            message = "Firestore repository does not support follow fasting. Use NetworkClientRepositoryImpl."
        )
    }
} 