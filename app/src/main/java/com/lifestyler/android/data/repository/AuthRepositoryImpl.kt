package com.lifestyler.android.data.repository

import com.lifestyler.android.domain.entity.AuthResult
import com.lifestyler.android.domain.entity.User
import com.lifestyler.android.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor() : AuthRepository {
    
    override suspend fun login(email: String, password: String): AuthResult {
        // Not implemented - app uses Registration Code + DOB login via ClientRepository
        return AuthResult.Error("Email/password login not supported. Please use Registration Code and DOB.")
    }
    
    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): AuthResult {
        // Not implemented - registration is handled via Google Forms
        return AuthResult.Error("Registration is handled via Google Forms. Please contact admin.")
    }
    
    override suspend fun logout(): Boolean {
        // Not implemented
        return true
    }
    
    override suspend fun getCurrentUser(): User? {
        // Not implemented
        return null
    }

    override suspend fun forgotPassword(email: String): AuthResult {
        // Not implemented
        return AuthResult.Error("Password reset not available. Please contact admin.")
    }

    override suspend fun verifyEmail(token: String): AuthResult {
        // Not implemented
        return AuthResult.Error("Email verification not available.")
    }

    override suspend fun isUserLoggedIn(): Boolean {
        // Not implemented
        return false
    }
}




