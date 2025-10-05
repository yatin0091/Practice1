package com.software.pandit.lyftlaptopinterview.data.network

import com.software.pandit.lyftlaptopinterview.BuildConfig
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Qualifier
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    @Qualifier
    @Retention(value = AnnotationRetention.BINARY)
    annotation class ApiBaseUrl

    @Qualifier
    @Retention(value = AnnotationRetention.BINARY)
    annotation class ApiKey

    @Provides
    @Singleton
    @ApiBaseUrl
    fun provideBaseUrl(): String = "https://api.unsplash.com/"

    @Provides
    @Singleton
    @ApiKey
    fun provideApiKey(): String {
        require(BuildConfig.UNSPLASH_ACCESS_KEY.isNotBlank()) {
            "Unsplash API key must be configured via Gradle property or UNSPLASH_ACCESS_KEY env var."
        }
        return BuildConfig.UNSPLASH_ACCESS_KEY
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()

    @Provides
    @Singleton
    fun provideRetrofit(
        @ApiBaseUrl baseUrl: String,
        client: OkHttpClient,
        moshi: Moshi
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun getPhotoApi(retrofit: Retrofit): PhotoApi = retrofit.create(PhotoApi::class.java)
}