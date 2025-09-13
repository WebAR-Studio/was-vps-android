package com.arstudio.wasvps_sdk.data.api

internal interface IWASVPSApiManager {

    fun getWASVPSApi(url: String, apiKey: String): WASVPSApi

}