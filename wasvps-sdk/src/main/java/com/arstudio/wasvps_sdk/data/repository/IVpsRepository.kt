package com.arstudio.wasvps_sdk.data.repository

import com.arstudio.wasvps_sdk.domain.model.LocalizationModel
import com.arstudio.wasvps_sdk.domain.model.WASVPSLocationModel

internal interface IWASVPSRepository {

    suspend fun requestLocalization(
        url: String,
        apiKey: String,
        vpsLocationModel: WASVPSLocationModel
    ): LocalizationModel?

}