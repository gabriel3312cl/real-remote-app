package com.remote.app.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.remote.app.billing.BillingManager
import com.remote.app.network.TVDiscoveryManager
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
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("TVSettings", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideBillingManager(@ApplicationContext context: Context): BillingManager {
        return BillingManager(context)
    }

    @Provides
    @Singleton
    fun provideTVDiscoveryManager(application: Application): TVDiscoveryManager {
        return TVDiscoveryManager(application)
    }
}
