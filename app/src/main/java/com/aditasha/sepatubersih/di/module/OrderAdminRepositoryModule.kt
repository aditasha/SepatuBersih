package com.aditasha.sepatubersih.di.module

import com.aditasha.sepatubersih.data.repository.OrderAdminRepositoryImpl
import com.aditasha.sepatubersih.domain.repository.OrderAdminRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OrderAdminRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindOrderRepository(orderAdminRepositoryImpl: OrderAdminRepositoryImpl): OrderAdminRepository
}