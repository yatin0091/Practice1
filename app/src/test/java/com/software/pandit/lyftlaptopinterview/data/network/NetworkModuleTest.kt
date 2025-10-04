package com.software.pandit.lyftlaptopinterview.data.network

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class NetworkModuleTest {

    private val module = NetworkModule()

    @Test
    fun `provideBaseUrl returns unsplash url`() {
        val baseUrl = module.provideBaseUrl()
        assertThat(baseUrl).isEqualTo("https://api.unsplash.com/")
    }

    @Test
    fun `provideApiKey returns expected key`() {
        val apiKey = module.provideApiKey()
        assertThat(apiKey).isEqualTo("V3sVfbkaOuBlwSD_BEqMSyJAB5gYectrTFl6-NrYyTM")
    }

    @Test
    fun `provideLoggingInterceptor sets body level`() {
        val interceptor = module.provideLoggingInterceptor()
        assertThat(interceptor).isInstanceOf(HttpLoggingInterceptor::class.java)
        assertThat(interceptor.level).isEqualTo(HttpLoggingInterceptor.Level.BODY)
    }

    @Test
    fun `provideOkHttpClient configures timeouts and interceptor`() {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS }
        val client = module.provideOkHttpClient(logging)
        assertThat(client.connectTimeoutMillis.toLong()).isEqualTo(20_000)
        assertThat(client.readTimeoutMillis.toLong()).isEqualTo(20_000)
        assertThat(client.writeTimeoutMillis.toLong()).isEqualTo(20_000)
        assertThat(client.interceptors).contains(logging)
    }

    @Test
    fun `provideMoshi returns builder instance`() {
        val moshi = module.provideMoshi()
        assertThat(moshi).isInstanceOf(Moshi::class.java)
    }

    @Test
    fun `provideRetrofit builds retrofit with moshi and client`() {
        val baseUrl = module.provideBaseUrl()
        val logging = module.provideLoggingInterceptor()
        val client = module.provideOkHttpClient(logging)
        val moshi = module.provideMoshi()

        val retrofit = module.provideRetrofit(baseUrl, client, moshi)

        assertThat(retrofit).isInstanceOf(Retrofit::class.java)
        assertThat(retrofit.baseUrl().toString()).isEqualTo(baseUrl)
        assertThat(retrofit.callFactory()).isEqualTo(client)
    }

    @Test
    fun `getPhotoApi creates retrofit service`() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.unsplash.com/")
            .client(OkHttpClient())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val api = module.getPhotoApi(retrofit)

        assertThat(api).isNotNull()
    }
}
