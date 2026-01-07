package com.lifestyler.android.domain.usecase

import com.lifestyler.android.domain.entity.AuthResult
import com.lifestyler.android.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    suspend operator fun invoke(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<AuthResult> {
        return try {
            val result = authRepository.register(email, password, firstName, lastName)
            Result.success(result)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
} 