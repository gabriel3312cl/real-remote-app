package com.remote.app.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.remote.app.data.billing.BillingManagerImpl
import com.remote.app.data.connection.TVConnectionManagerImpl
import com.remote.app.data.network.TVDiscoveryManagerImpl
import com.remote.app.data.settings.SettingsRepositoryImpl
import com.remote.app.domain.repository.BillingRepository
import com.remote.app.domain.repository.SettingsRepository
import com.remote.app.domain.repository.TVConnectionRepository
import com.remote.app.domain.repository.TVDiscoveryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppProviderModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("TVSettings", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideApplicationContext(application: Application): Context {
        return application.applicationContext
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindsModule {

    @Binds
    @Singleton
    abstract fun bindTVConnectionRepository(impl: TVConnectionManagerImpl): TVConnectionRepository

    @Binds
    @Singleton
    abstract fun bindTVDiscoveryRepository(impl: TVDiscoveryManagerImpl): TVDiscoveryRepository

    @Binds
    @Singleton
    abstract fun bindBillingRepository(impl: BillingManagerImpl): BillingRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
