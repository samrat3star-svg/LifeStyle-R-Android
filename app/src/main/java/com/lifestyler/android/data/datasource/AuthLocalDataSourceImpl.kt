package com.lifestyler.android.data.datasource

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.lifestyler.android.domain.entity.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthLocalDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : AuthLocalDataSource {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )

    override suspend fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit().putString(KEY_USER, userJson).apply()
    }

    override suspend fun getCurrentUserSync(): User? {
        val userJson = sharedPreferences.getString(KEY_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    override fun getCurrentUser(): Flow<User?> = flow {
        emit(getCurrentUserSync())
    }

    override suspend fun clearUser() {
        sharedPreferences.edit().remove(KEY_USER).apply()
    }

    companion object {
        private const val PREF_NAME = "auth_preferences"
        private const val KEY_USER = "current_user"
    }
} 