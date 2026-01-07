package com.lifestyler.android.data.datasource

import com.lifestyler.android.domain.entity.AuthResult

interface AuthRemoteDataSource {
    suspend fun loginUser(email: String, password: String): AuthResult
    suspend fun registerUser(email: String, password: String, firstName: String, lastName: String): AuthResult
    suspend fun forgotPassword(email: String): AuthResult
    suspend fun verifyEmail(token: String): AuthResult
} 