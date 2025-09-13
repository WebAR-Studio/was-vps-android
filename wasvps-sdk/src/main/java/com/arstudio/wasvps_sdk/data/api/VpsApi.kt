package com.arstudio.wasvps_sdk.data.api

import com.arstudio.wasvps_sdk.data.model.response.ResponseVpsModel
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

internal interface WASVPSApi {

    @Multipart
    @POST("vps/api/v3")
    suspend fun requestLocalization(@Part vararg parts: MultipartBody.Part): ResponseVpsModel

}