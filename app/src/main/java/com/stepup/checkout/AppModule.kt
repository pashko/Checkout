package com.stepup.checkout

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.stepup.checkout.repo.CheckoutService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    companion object {
        private const val BASE_URL = "https://integrations-cko.herokuapp.com/"
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideService(retrofit: Retrofit): CheckoutService =
        retrofit.create(CheckoutService::class.java)
}