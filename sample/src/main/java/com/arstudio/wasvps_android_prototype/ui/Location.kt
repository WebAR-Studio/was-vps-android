package com.arstudio.wasvps_android_prototype.ui

import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3

sealed interface Location {
    val locationIds: Array<String>
    val localPosition: Vector3
    val localRotation: Quaternion
    val scale: Vector3

    object Mariel : Location {
        override val locationIds: Array<String> = arrayOf("mariel")
        override val localPosition: Vector3 = Vector3.zero()
        override val localRotation: Quaternion = Quaternion.identity()
        override val scale: Vector3 = Vector3.one()
    }

}