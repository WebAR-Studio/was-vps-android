package com.arstudio.wasvps_sdk.data.model.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.arstudio.wasvps_sdk.data.model.LocationModel
import com.arstudio.wasvps_sdk.data.model.PoseModel

@JsonClass(generateAdapter = true)
internal data class ResponseAttributesModel(
    @Json(name = "location_id")
    val locationId: String,
    @Json(name = "location")
    val location: LocationModel?,
    @Json(name = "tracking_pose")
    val trackingPose: PoseModel?,
    @Json(name = "vps_pose")
    val vpsPose: PoseModel?
)
