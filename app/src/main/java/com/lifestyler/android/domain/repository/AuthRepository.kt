package com.lifestyler.android.domain.repository

import com.lifestyler.android.domain.entity.AuthResult
import com.lifestyler.android.domain.entity.User

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult
    suspend fun register(email: String, password: String, firstName: String, lastName: String): AuthResult
    suspend fun logout(): Boolean
    suspend fun getCurrentUser(): User?
    suspend fun forgotPassword(email: String): AuthResult
    suspend fun verifyEmail(token: String): AuthResult
    suspend fun isUserLoggedIn(): Boolean
} 