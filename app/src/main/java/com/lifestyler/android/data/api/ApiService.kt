package com.lifestyler.android.data.api

import com.lifestyler.android.data.model.ClientsResponse
import com.lifestyler.android.data.model.ClientUpdateRequest
import com.lifestyler.android.data.model.ClientUpdateResponse
import com.lifestyler.android.data.model.PendingClientsResponse
import com.lifestyler.android.data.model.LoginRequest
import com.lifestyler.android.data.model.LoginResponse
import com.lifestyler.android.data.model.FastingSettingsResponse
import com.lifestyler.android.data.model.LogFastingRequest
import com.lifestyler.android.data.model.LogFastingResponse
import com.lifestyler.android.domain.entity.PendingClient
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {
    @GET("exec?action=getPendingClients")
    suspend fun getPendingClients(): Response<PendingClientsResponse>
    
    @GET("exec?action=getAllClients")
    suspend fun getAllClients(): Response<ClientsResponse>
    
    @POST("exec?action=updateClient")
    suspend fun updateClient(
        @Body request: ClientUpdateRequest
    ): Response<ClientUpdateResponse>

    @GET("exec")
    suspend fun login(
        @Query("action") action: String,
        @Query("regCode") regCode: String,
        @Query("mobile") mobile: String
    ): Response<LoginResponse>

    @GET("exec")
    suspend fun getClientSettings(
        @Query("action") action: String,
        @Query("sheetName") sheetName: String
    ): Response<FastingSettingsResponse>

    @GET("exec")
    suspend fun logFasting(
        @Query("action") action: String,
        @Query("sheetName") sheetName: String,
        @Query("duration") duration: String
    ): Response<LogFastingResponse>

    @GET("exec")
    suspend fun followFasting(
        @Query("action") action: String,
        @Query("sheetName") sheetName: String,
        @Query("fastingEndDate") fastingEndDate: String? = null
    ): Response<LogFastingResponse>

    @GET("exec")
    suspend fun breakFasting(
        @Query("action") action: String,
        @Query("sheetName") sheetName: String,
        @Query("deviceTime") deviceTime: String
    ): Response<LogFastingResponse>

    @GET("exec")
    suspend fun getMeasurements(
        @Query("action") action: String,
        @Query("sheetName") sheetName: String
    ): Response<com.lifestyler.android.data.model.MeasurementsResponse>

    @GET("exec")
    suspend fun getBreaks(
        @Query("action") action: String,
        @Query("sheetName") sheetName: String
    ): Response<com.lifestyler.android.data.model.BreaksResponse>
}