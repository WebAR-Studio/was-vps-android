package com.arstudio.wasvps_android_prototype.ui

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.RawRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.ar.core.Config
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.rendering.EngineInstance
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.arstudio.wasvps_android_prototype.R
import com.arstudio.wasvps_android_prototype.databinding.FmtSceneBinding
import com.arstudio.wasvps_android_prototype.databinding.MenuSceneBinding
import com.arstudio.wasvps_android_prototype.util.Logger
import com.arstudio.wasvps_sdk.common.CoordinateConverter
import com.arstudio.wasvps_sdk.data.WASVPSConfig
import com.arstudio.wasvps_sdk.domain.model.GpsPoseModel
import com.arstudio.wasvps_sdk.ui.WASVPSArFragment
import com.arstudio.wasvps_sdk.ui.WASVPSCallback
import com.arstudio.wasvps_sdk.ui.WASVPSService
import kotlinx.coroutines.delay
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController.Visibility
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class SceneFragment : Fragment(R.layout.fmt_scene), WASVPSCallback, Scene.OnUpdateListener {

    private companion object {
        const val INDICATOR_COLOR_DELAY = 500L
        const val BASE_COLOR_FACTOR = "baseColorFactor"
    }

    private val location: Location = Location.Mariel

    // Replace "your-api-key-here" with your real API key
    private var vpsConfig: WASVPSConfig = WASVPSConfig.getOutdoorConfig("your-api-key-here", location.locationIds)

    private val robotNode: Node = Node()

    private val binding: FmtSceneBinding by viewBinding(FmtSceneBinding::bind)

    private val vpsArFragment: WASVPSArFragment
        get() = childFragmentManager.findFragmentById(binding.vFragmentContainer.id) as WASVPSArFragment

    private val vMap: MapView
        get() = binding.vMap

    private val vpsService: WASVPSService
        get() = vpsArFragment.vpsService

    private val coordinateConverter: CoordinateConverter by lazy {
        CoordinateConverter.instance()
    }

    private val marker: Marker by lazy {
        Marker(vMap)
            .apply {
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_heading)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                vMap.overlays.add(this)
                vMap.controller.setZoom(18.0)
            }
    }

    private var errorDialog: Dialog? = null

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.vTouchZone.setOnLongClickListener {
            showMenu()
            true
        }

        initWASVPSService()

        loadModel(R.raw.robot) {
            robotNode.renderable = it
            vpsService.worldNode
                .addChild(robotNode)

            setupRobotModel()
        }
        vpsArFragment.setOnSessionConfigurationListener { session, config ->
            config.focusMode = Config.FocusMode.FIXED
            session.resume()
        }

        vpsService.startWASVPSService()

        with(vMap) {
            setTileSource(TileSourceFactory.MAPNIK)
            zoomController.setVisibility(Visibility.NEVER)
        }
    }

    override fun onStart() {
        super.onStart()
        vpsArFragment.arSceneView.scene.addOnUpdateListener(this)
    }

    override fun onResume() {
        super.onResume()
        vMap.onResume()
    }

    override fun onPause() {
        super.onPause()
        vMap.onPause()
    }

    override fun onStop() {
        super.onStop()
        vpsArFragment.arSceneView.scene.removeOnUpdateListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vMap.onDetach()
    }

    override fun onSuccess() {
        updateVpsStatus(true)
    }

    override fun onFail() {
        updateVpsStatus(false)
    }

    override fun onStateChange(state: WASVPSService.State) {
        Logger.debug("VPS service: $state")
    }

    override fun onError(error: Throwable) {
        Logger.error(error)
        showError(error)
    }

    override fun onUpdate(frameTime: FrameTime?) {
        val gpsPose = coordinateConverter.convertToGlobalCoordinate(vpsService.cameraPose)
        if (gpsPose == GpsPoseModel.EMPTY) return

        marker.position = GeoPoint(gpsPose.latitude, gpsPose.longitude)
        marker.rotation = gpsPose.heading
        vMap.controller.setCenter(marker.position)
    }

    private fun loadModel(@RawRes rawRes: Int, completeCallback: (Renderable) -> Unit) {
        ModelRenderable.builder()
            .setSource(context, rawRes)
            .setIsFilamentGltf(true)
            .build()
            .thenApply(completeCallback)
            .exceptionally { Logger.error(it) }
    }


    private fun showMenu() {
        val menuBinding = MenuSceneBinding.inflate(layoutInflater)

        menuBinding.cbAutofocus.isChecked = vpsArFragment.isAutofocus()
        menuBinding.cbGps.isChecked = vpsConfig.useGps

        AlertDialog.Builder(requireContext())
            .setView(menuBinding.root)
            .setPositiveButton(R.string.apply) { _, _ ->
                vpsArFragment.setAutofocus(menuBinding.cbAutofocus.isChecked)

                restartWASVPSService(
                    menuBinding.cbGps.isChecked,
                )
            }
            .show()
    }

    private fun initWASVPSService() {
        with(vpsService) {
            setWASVPSCallback(this@SceneFragment)
            setWASVPSConfig(vpsConfig)
        }
    }


    private fun setupRobotModel() {
        with(robotNode) {
            localPosition = location.localPosition
            localRotation = location.localRotation
            localScale = location.scale

            renderableInstance.animate(true)
                .start()
        }
    }

    private fun restartWASVPSService(
        gpsEnable: Boolean,
    ) {
        vpsService.stopWASVPSService()

        vpsConfig = vpsConfig.copy(
            useGps = gpsEnable
        )
        vpsService.setWASVPSConfig(vpsConfig)

        vpsService.startWASVPSService()
    }

    private fun updateVpsStatus(isSuccess: Boolean) {
        val indicatorColor = if (isSuccess) Color.GREEN else Color.RED
        binding.vIndicator.background.setTint(indicatorColor)

        lifecycleScope.launchWhenCreated {
            delay(INDICATOR_COLOR_DELAY)
            binding.vIndicator.background.setTint(Color.WHITE)
        }
    }

    private fun showError(e: Throwable) {
        errorDialog?.let { it.findViewById<TextView>(android.R.id.message).text = e.toString() }
            ?.also { return }

        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(e.toString())
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .setOnDismissListener { errorDialog = null }
            .show()
            .also { errorDialog = it }
    }

}