package com.arstudio.wasvps_sdk.data

data class WASVPSConfig(
    val vpsUrl: String = "https://was-vps.web-ar.xyz/",
    val apiKey: String,
    val locationIds: Array<String>,
    val intervalLocalizationMS: Long = 2500,
    val useGps: Boolean = false,
    val failsCountToResetSession: Int = 5,
    val updateWorldDurationMS: Long = 500,
    val updateWorldDistanceLimit: Float = 2f,
    val updateWorldAngleLimit: Float = 10f
) {
    companion object {

        fun getIndoorConfig(apiKey: String, locationIds: Array<String>): WASVPSConfig =
            WASVPSConfig(
                apiKey = apiKey,
                locationIds = locationIds,
                useGps = false
            )

        fun getOutdoorConfig(apiKey: String, locationIds: Array<String>): WASVPSConfig =
            WASVPSConfig(
                apiKey = apiKey,
                locationIds = locationIds,
                useGps = true
            )

    }
}