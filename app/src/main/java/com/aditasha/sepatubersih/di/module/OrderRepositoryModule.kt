package com.aditasha.sepatubersih.di.module

import com.aditasha.sepatubersih.data.repository.OrderRepositoryImpl
import com.aditasha.sepatubersih.domain.repository.OrderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OrderRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindOrderRepository(orderRepositoryImpl: OrderRepositoryImpl): OrderRepository
}