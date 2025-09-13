package com.arstudio.wasvps_sdk.ui

import com.arstudio.wasvps_sdk.ui.WASVPSService.State

interface WASVPSCallback {
    fun onSuccess()
    fun onFail()
    fun onStateChange(state: State)
    fun onError(error: Throwable)
}