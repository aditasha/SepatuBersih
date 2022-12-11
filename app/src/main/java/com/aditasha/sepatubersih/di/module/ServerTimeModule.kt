package com.aditasha.sepatubersih.di.module

import com.aditasha.sepatubersih.ServerTime
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ServerTimeModule {

    @Provides
    @Singleton
    fun provideServerTime(firebaseDatabase: FirebaseDatabase): ServerTime {
        return ServerTime(firebaseDatabase)
    }
}