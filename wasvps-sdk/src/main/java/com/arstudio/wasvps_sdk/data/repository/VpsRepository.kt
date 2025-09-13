package com.arstudio.wasvps_sdk.data.repository

import com.squareup.moshi.JsonAdapter
import com.arstudio.wasvps_sdk.data.api.IWASVPSApiManager
import com.arstudio.wasvps_sdk.data.model.CompassModel
import com.arstudio.wasvps_sdk.data.model.GpsModel
import com.arstudio.wasvps_sdk.data.model.LocationModel
import com.arstudio.wasvps_sdk.data.model.PoseModel
import com.arstudio.wasvps_sdk.data.model.request.RequestAttributesModel
import com.arstudio.wasvps_sdk.data.model.request.RequestDataModel
import com.arstudio.wasvps_sdk.data.model.request.RequestIntrinsicsModel
import com.arstudio.wasvps_sdk.data.model.request.RequestVpsModel
import com.arstudio.wasvps_sdk.domain.model.GpsPoseModel
import com.arstudio.wasvps_sdk.domain.model.LocalizationModel
import com.arstudio.wasvps_sdk.domain.model.NodePoseModel
import com.arstudio.wasvps_sdk.domain.model.WASVPSLocationModel
import com.arstudio.wasvps_sdk.util.Logger
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException


internal class WASVPSRepository(
    private val vpsApiManager: IWASVPSApiManager,
    private val requestVpsAdapter: JsonAdapter<RequestVpsModel>,
) : IWASVPSRepository {

    private companion object {
        const val STATUS_DONE = "done"

        const val EMBEDDING = "embedding"
        const val IMAGE = "image"
        const val JSON = "json"

        val ANY_MEDIA_TYPE = "*/*".toMediaTypeOrNull()
        val IMAGE_MEDIA_TYPE = "image/jpeg".toMediaTypeOrNull()
        val JSON_MEDIA_TYPE = "application/json".toMediaTypeOrNull()
    }

    override suspend fun requestLocalization(
        url: String,
        apiKey: String,
        vpsLocationModel: WASVPSLocationModel
    ): LocalizationModel? {
        val vpsApi = vpsApiManager.getWASVPSApi(url, apiKey)

        val jsonBody = vpsLocationModel.toRequestVpsModel()
            .toBodyPart()

        val contentBody = vpsLocationModel.toBodyPart(IMAGE_MEDIA_TYPE, IMAGE)

        val response = try {
            vpsApi.requestLocalization(jsonBody, contentBody)
        }catch (e: HttpException){
            var msg: String = ""
            val jObjError = JSONObject(e.response()?.errorBody()?.string().toString())
            when(e.code()){
                500,404 -> {
                    msg = jObjError.getString("detail")
                }
                422 -> {
                    val details: JSONArray = jObjError.getJSONArray("detail")
                    for (i in 0 until details.length()){
                        val detail = details.getJSONObject(i)
                        val locs: JSONArray = detail.getJSONArray("loc")
                        for(j in 0 until locs.length()){
                            val loc = locs.getString(j)
                            msg += "$loc "
                        }
                        msg += "\n" + detail.getString("msg") + "\n" + detail.getString("type")
                    }
                }

            }
            Logger.error(msg)
            return null
        }catch (e: Exception) {
            Logger.error(e)
            return null
        }

        val dataModel = response.data
        val attributesModel = dataModel?.attributes
        if (dataModel?.status == STATUS_DONE) {
            val vpsPoseModel = attributesModel?.vpsPose
                .toNodePoseModel()
            val trackingPoseModel = attributesModel?.trackingPose
                .toNodePoseModel()
            val gpsPoseModel = attributesModel?.location
                .toGpsPoseModel()
            return LocalizationModel(vpsPoseModel, trackingPoseModel, gpsPoseModel)
        }
        Logger.error(dataModel?.statusDescription)
        return null
    }

    private fun WASVPSLocationModel.toRequestVpsModel(): RequestVpsModel =
        RequestVpsModel(
            data = RequestDataModel(
                attributes = RequestAttributesModel(
                    locationIds = this.locationIds,
                    sessionId = this.sessionId,
                    userId = this.userId,
                    timestamp = this.timestamp,
                    location = this.gpsLocation?.let { gpsLocation ->
                        LocationModel(
                            gps = GpsModel(
                                accuracy = gpsLocation.accuracy,
                                altitude = gpsLocation.altitude,
                                latitude = gpsLocation.latitude,
                                longitude = gpsLocation.longitude,
                                timestamp = gpsLocation.elapsedTimestampSec
                            ),
                            compass = CompassModel(
                                accuracy = compass.accuracy,
                                heading = compass.heading,
                                timestamp = compass.timestamp
                            )
                        )
                    },
                    trackingPose = PoseModel(
                        x = this.trackingPose.x,
                        y = this.trackingPose.y,
                        z = this.trackingPose.z,
                        rx = this.trackingPose.rx,
                        ry = this.trackingPose.ry,
                        rz = this.trackingPose.rz
                    ),
                    intrinsics = RequestIntrinsicsModel(
                        width = cameraIntrinsics.width,
                        height = cameraIntrinsics.height,
                        cx = cameraIntrinsics.cx,
                        cy = cameraIntrinsics.cy,
                        fx = cameraIntrinsics.fx,
                        fy = cameraIntrinsics.fy
                    )
                )
            )
        )

    private fun RequestVpsModel.toBodyPart(): MultipartBody.Part {
        val requestBody = requestVpsAdapter.toJson(this).toRequestBody(JSON_MEDIA_TYPE)
        return MultipartBody.Part.createFormData(JSON, null, requestBody)
    }

    private fun WASVPSLocationModel.toBodyPart(
        contentType: MediaType?,
        name: String,
        fileName: String? = name
    ): MultipartBody.Part {
        val requestBody = byteArray.toRequestBody(contentType, 0, byteArray.size)
        return MultipartBody.Part.createFormData(name, fileName, requestBody)
    }

    private fun PoseModel?.toNodePoseModel(): NodePoseModel =
        if (this == null)
            NodePoseModel.DEFAULT
        else
            NodePoseModel(
                x = this.x,
                y = this.y,
                z = this.z,
                rx = this.rx,
                ry = this.ry,
                rz = this.rz,
            )

    private fun LocationModel?.toGpsPoseModel(): GpsPoseModel =
        if (this == null)
            GpsPoseModel.EMPTY
        else
            GpsPoseModel(
                altitude = gps.altitude,
                latitude = gps.latitude,
                longitude = gps.longitude,
                heading = compass?.heading ?: 0f
            )

}