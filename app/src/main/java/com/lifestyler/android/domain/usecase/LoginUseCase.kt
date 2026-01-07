package com.lifestyler.android.domain.usecase

import com.lifestyler.android.data.model.LoginResponse
import com.lifestyler.android.domain.repository.ClientRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    
    suspend operator fun invoke(regCode: String, mobile: String): Result<LoginResponse> {
        return try {
            val result = clientRepository.login(regCode, mobile)
            if (result.success) {
                Result.success(result)
            } else {
                Result.failure(Exception(result.message ?: "Login failed"))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
} 