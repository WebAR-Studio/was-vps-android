package com.arstudio.wasvps_sdk.domain.interactor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import com.arstudio.wasvps_sdk.data.model.CameraIntrinsics
import com.arstudio.wasvps_sdk.data.repository.IPrefsRepository
import com.arstudio.wasvps_sdk.data.repository.IWASVPSRepository
import com.arstudio.wasvps_sdk.domain.model.*
import com.arstudio.wasvps_sdk.util.Constant.BITMAP_WIDTH
import com.arstudio.wasvps_sdk.util.Constant.MATRIX_ROTATE
import com.arstudio.wasvps_sdk.util.Constant.QUALITY
import com.arstudio.wasvps_sdk.util.TimestampUtil
import com.arstudio.wasvps_sdk.util.cropTo16x9
import com.arstudio.wasvps_sdk.util.toGrayscale
import java.io.ByteArrayOutputStream

internal class WASVPSInteractor(
    private val vpsRepository: IWASVPSRepository,
    private val prefsRepository: IPrefsRepository
) : IWASVPSInteractor {

    private var scaleFactorPhoto: Float = 1f

    private var imageWidth: Int = 1080
    private var imageHeight: Int = 1920

    override suspend fun prepareWASVPSLocationModel(
        locationIds: Array<String>,
        source: ByteArray,
        sessionId: String,
        cameraPose: NodePoseModel,
        gpsLocation: GpsLocationModel?,
        compass: CompassModel,
        cameraIntrinsics: CameraIntrinsics
    ): WASVPSLocationModel {
        val byteArray = createJpgByteArray(source)
        val newCameraIntrinsics = cameraIntrinsics.scaleCameraIntrinsics()

        return WASVPSLocationModel(
            locationIds = locationIds,
            sessionId = sessionId,
            userId = prefsRepository.getUserId(),
            timestamp = TimestampUtil.getTimestampInSec(),
            gpsLocation = gpsLocation,
            compass = compass,
            trackingPose = cameraPose,
            byteArray = byteArray,
            cameraIntrinsics = newCameraIntrinsics
        )
    }

    override suspend fun calculateNodePose(
        url: String,
        apiKey: String,
        vpsLocationModel: WASVPSLocationModel
    ): LocalizationModel? =
        vpsRepository.requestLocalization(url, apiKey, vpsLocationModel)

    override fun destroy() {
        // No-op
    }

    

    private fun createJpgByteArray(byteArray: ByteArray): ByteArray {
        val source = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            .cropTo16x9()
        scaleFactorPhoto = BITMAP_WIDTH.toFloat() / source.width

        val matrix = Matrix()
            .apply {
                postRotate(MATRIX_ROTATE)
                postScale(scaleFactorPhoto, scaleFactorPhoto)
            }
        val bitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
            .toGrayscale()

        return ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, stream)
            stream.toByteArray()
        }
    }

    private fun CameraIntrinsics.scaleCameraIntrinsics(): CameraIntrinsics {
        val scale = scaleFactorPhoto
        return this.copy(
            width = (imageWidth * scale).toInt(),
            height = (imageHeight * scale).toInt(),
            fx = fx * scale,
            fy = fy * scale,
            cx = cx * scale,
            cy = cy * scale
        )
    }
}