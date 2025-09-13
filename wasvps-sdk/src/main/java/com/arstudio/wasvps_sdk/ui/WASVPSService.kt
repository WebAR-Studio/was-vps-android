package com.arstudio.wasvps_sdk.ui

import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.arstudio.wasvps_sdk.WASVPSSdkInitializationException
import com.arstudio.wasvps_sdk.data.WASVPSConfig
import com.arstudio.wasvps_sdk.domain.model.NodePoseModel
import org.koin.core.context.GlobalContext

interface WASVPSService {

    companion object {

        @JvmStatic
        fun newInstance(): WASVPSService {
            GlobalContext.getOrNull() ?: throw WASVPSSdkInitializationException()

            return GlobalContext.get().get()
        }
    }

    val worldNode: Node
    val isRun: Boolean
    val cameraPose: NodePoseModel

    fun bindArSceneView(arSceneView: ArSceneView)
    fun resume()
    fun pause()
    fun destroy()

    fun setWASVPSConfig(vpsConfig: WASVPSConfig)
    fun setWASVPSCallback(vpsCallback: WASVPSCallback)

    fun startWASVPSService()
    fun stopWASVPSService()

    enum class State {
        RUN, PAUSE, STOP
    }

}