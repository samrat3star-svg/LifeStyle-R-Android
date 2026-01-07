package com.lifestyler.android.data.datasource

import com.lifestyler.android.domain.entity.User
import kotlinx.coroutines.flow.Flow

interface AuthLocalDataSource {
    suspend fun saveUser(user: User)
    suspend fun getCurrentUserSync(): User?
    fun getCurrentUser(): Flow<User?>
    suspend fun clearUser()
} 