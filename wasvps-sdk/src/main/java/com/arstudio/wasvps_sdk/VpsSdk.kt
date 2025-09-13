package com.arstudio.wasvps_sdk

import android.content.Context
import com.arstudio.wasvps_sdk.di.Module
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

object WASVPSSdk {

    fun init(context: Context) {
        if (GlobalContext.getOrNull() != null) return

        startKoin {
            androidContext(context)
            modules(
                Module.repository,
                Module.domain,
                Module.presentation
            )
        }
    }

}

class WASVPSSdkInitializationException : RuntimeException("Must be called WASVPSSdk.init(context)")