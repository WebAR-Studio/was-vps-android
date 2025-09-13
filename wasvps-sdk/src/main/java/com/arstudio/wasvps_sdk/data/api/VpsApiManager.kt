package com.arstudio.wasvps_sdk.data.api

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal class WASVPSApiManager(
    private val defaultClient: OkHttpClient,
    private val moshi: Moshi
) : IWASVPSApiManager {

    private val cacheWASVPSApi: MutableMap<String, WASVPSApi> = mutableMapOf()

    override fun getWASVPSApi(url: String, apiKey: String): WASVPSApi =
        cacheWASVPSApi[url]
            ?: getClient(url, apiKey)
                .create(WASVPSApi::class.java)
                .also { cacheWASVPSApi[url] = it }

    private fun getClient(baseUrl: String, apiKey: String): Retrofit {
        val okHttpClient = defaultClient.newBuilder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("x-vps-apikey", apiKey)
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

}