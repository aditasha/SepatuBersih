package com.aditasha.sepatubersih.di.module

import com.aditasha.sepatubersih.data.repository.OrderDriverRepositoryImpl
import com.aditasha.sepatubersih.domain.repository.OrderDriverRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OrderDriverRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindOrderRepository(orderDriverRepositoryImpl: OrderDriverRepositoryImpl): OrderDriverRepository
}