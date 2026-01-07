package com.lifestyler.android.data.datasource

import com.lifestyler.android.domain.entity.AuthResult
import com.lifestyler.android.domain.entity.User
import kotlinx.coroutines.delay
import javax.inject.Inject

class AuthRemoteDataSourceImpl @Inject constructor() : AuthRemoteDataSource {

    override suspend fun loginUser(email: String, password: String): AuthResult {
        delay(1000) // Simulate network delay
        
        return if (email == "test@example.com" && password == "password") {
            AuthResult.Success(
                User(
                    userId = "1",
                    email = email,
                    name = "John Doe",
                    phone = "",
                    dateCreated = System.currentTimeMillis().toString()
                )
            )
        } else {
            AuthResult.Error("Invalid email or password")
        }
    }

    override suspend fun registerUser(email: String, password: String, firstName: String, lastName: String): AuthResult {
        delay(1000) // Simulate network delay
        
        return AuthResult.Success(
            User(
                userId = "2",
                email = email,
                name = "$firstName $lastName",
                phone = "",
                dateCreated = System.currentTimeMillis().toString()
            )
        )
    }

    override suspend fun forgotPassword(email: String): AuthResult {
        delay(1000) // Simulate network delay
        return AuthResult.Success(
            User()
        )
    }

    override suspend fun verifyEmail(token: String): AuthResult {
        delay(1000) // Simulate network delay
        return AuthResult.Success(
            User()
        )
    }
} 