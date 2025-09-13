package com.arstudio.wasvps_sdk.domain.interactor

import com.arstudio.wasvps_sdk.data.model.CameraIntrinsics
import com.arstudio.wasvps_sdk.domain.model.*

internal interface IWASVPSInteractor {

    suspend fun prepareWASVPSLocationModel(
        locationIds: Array<String>,
        source: ByteArray,
        sessionId: String,
        cameraPose: NodePoseModel,
        gpsLocation: GpsLocationModel? = null,
        compass: CompassModel,
        cameraIntrinsics: CameraIntrinsics
    ): WASVPSLocationModel

    suspend fun calculateNodePose(
        url: String,
        apiKey: String,
        vpsLocationModel: WASVPSLocationModel
    ): LocalizationModel?

    fun destroy()

}