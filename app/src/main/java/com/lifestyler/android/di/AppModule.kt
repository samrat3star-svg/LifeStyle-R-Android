package com.lifestyler.android.di

import android.content.Context
import com.lifestyler.android.data.preference.PreferenceManager
import com.lifestyler.android.data.repository.UpdateRepositoryImpl
import com.lifestyler.android.domain.repository.AuthRepository
import com.lifestyler.android.domain.repository.ClientRepository
import com.lifestyler.android.domain.repository.UpdateRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun providePreferenceManager(@ApplicationContext context: Context): PreferenceManager {
        return PreferenceManager(context)
    }
    
    @Provides
    @Singleton
    fun provideClientRepository(networkRepository: NetworkClientRepositoryImpl): ClientRepository {
        return networkRepository
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository {
        return authRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideUpdateRepository(updateRepositoryImpl: UpdateRepositoryImpl): UpdateRepository {
        return updateRepositoryImpl
    }

    @Provides
    @Singleton
    fun providePollingManager(@ApplicationContext context: Context, preferenceManager: PreferenceManager): com.lifestyler.android.data.manager.PollingManager {
        return com.lifestyler.android.data.manager.PollingManager(context, preferenceManager)
    }
} 