package com.arstudio.wasvps_android_prototype

import android.app.Application
import com.arstudio.wasvps_sdk.WASVPSSdk
import org.osmdroid.config.Configuration

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        WASVPSSdk.init(this)

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

}