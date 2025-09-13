package com.arstudio.wasvps_sdk.di

import android.content.Context
import android.hardware.SensorManager
import android.location.LocationManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.arstudio.wasvps_sdk.common.CompassManager
import com.arstudio.wasvps_sdk.common.CoordinateConverter
import com.arstudio.wasvps_sdk.data.api.IWASVPSApiManager
 
import com.arstudio.wasvps_sdk.data.api.WASVPSApiManager
import com.arstudio.wasvps_sdk.data.model.request.RequestVpsModel
import com.arstudio.wasvps_sdk.data.repository.*
import com.arstudio.wasvps_sdk.domain.interactor.IWASVPSInteractor
import com.arstudio.wasvps_sdk.domain.interactor.WASVPSInteractor
import com.arstudio.wasvps_sdk.ui.ArManager
import com.arstudio.wasvps_sdk.ui.WASVPSArViewModel
import com.arstudio.wasvps_sdk.ui.WASVPSService
import com.arstudio.wasvps_sdk.ui.WASVPSServiceImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.koin.core.module.Module
import org.koin.dsl.module
 

internal object Module {

    private const val HOST_MOCK = "http://mock/"

    val repository: Module = module {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
            .apply { level = if (com.arstudio.wasvps_sdk.BuildConfig.DEBUG) Level.BASIC else Level.NONE }

        single {
            OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build()
        }
        single<Moshi> {
            Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
        }
        single<JsonAdapter<RequestVpsModel>> { get<Moshi>().adapter(RequestVpsModel::class.java) }
        single<IWASVPSApiManager> { WASVPSApiManager(get(), get()) }
        factory<IWASVPSRepository> { WASVPSRepository(get(), get()) }

        single<IPrefsRepository> { PrefsRepository(get()) }
    }

    val domain: Module = module {
        factory<IWASVPSInteractor> { WASVPSInteractor(get(), get()) }
    }

    val presentation: Module = module {
        factory { ArManager() }
        factory { get<Context>().getSystemService(LocationManager::class.java) }
        factory { get<Context>().getSystemService(SensorManager::class.java) }
        factory { CompassManager(get()) }
        single { CoordinateConverter() }
        factory<WASVPSService> { WASVPSServiceImpl(get(), get(), get(), get(), get()) }
        factory { WASVPSArViewModel(get(), get(), get()) }
    }

}