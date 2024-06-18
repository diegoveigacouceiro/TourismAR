package es.itg.tourismar.ui.screens.arscreen.controllers

import android.animation.ValueAnimator
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.Color
import com.google.android.filament.Engine
import com.google.android.filament.View
import com.google.ar.core.DepthPoint
import com.google.ar.core.Earth
import com.google.ar.core.Frame
import com.google.ar.core.Point
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import dev.romainguy.kotlin.math.Float3
import es.itg.tourismar.data.model.anchor.Anchor
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.data.model.anchor.CustomLatLng
import es.itg.tourismar.data.model.anchor.HostingState
import es.itg.tourismar.data.model.anchor.ScanningState
import es.itg.tourismar.data.model.marker.Marker
import es.itg.tourismar.data.model.marker.MarkerRoute
import es.itg.tourismar.ui.screens.arscreen.ARSceneViewModel
import io.github.sceneview.ar.camera.ARCameraStream
import io.github.sceneview.ar.node.ARCameraNode
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.node.CloudAnchorNode
import io.github.sceneview.collision.CollisionSystem
import io.github.sceneview.collision.Plane
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.model.isShadowReceiver
import io.github.sceneview.model.setScreenSpaceContactShadows
import io.github.sceneview.node.LightNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.utils.loadFileBuffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.sceneview.geometries.Cube
import io.github.sceneview.math.Size
import io.github.sceneview.node.CubeNode


class ARSceneController(
    val engine: Engine,
    val modelLoader: ModelLoader,
    val materialLoader: MaterialLoader,
    val cameraNode: ARCameraNode,
    val cameraStream: ARCameraStream,
    var view: View,
    val collisionSystem: CollisionSystem,
    val context: Context,
    val mainLightNode: LightNode,
    val arSceneViewModel: ARSceneViewModel,
    var frame: Frame?,
    var session: Session?,
    var trackingFailureReason: TrackingFailureReason?,
) {
    val childNodes = SnapshotStateList<Pair<String?, Node>>()
    var planeRenderer by mutableStateOf(true)
    var isLoading by mutableStateOf(false)
    var isTesting by mutableStateOf(false)
    val mainScope = CoroutineScope(Dispatchers.Main)
    val ioScope = CoroutineScope(Dispatchers.IO)

    var scanningState by mutableStateOf(ScanningState.SCANNING)
    var scanningMessage by mutableStateOf("Móvete co dispositivo enfocando o entorno para escanealo.")
    var hostingState by  mutableStateOf(HostingState.PLACING)
    var placedAnchor by mutableStateOf<com.google.ar.core.Anchor?>(null)
    var resolvedAnchors = SnapshotStateMap<String, Pair<Anchor, Boolean>>()
    var resolvedMarkers = SnapshotStateMap<CustomLatLng, Pair<Marker, Boolean>>()



    /**
     * ARSCENE HOSTING FUNCTIONS
     **/

    fun handleHosting(anchor: com.google.ar.core.Anchor, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        hostingState = HostingState.HOSTING
        hostCloudAnchor(anchor,
            onSuccess = { cloudAnchorId ->
                Log.d("ARSceneScreen", "Successfully hosted cloud anchor with ID: $cloudAnchorId")
                hostingState = HostingState.PLACING
                onSuccess(cloudAnchorId)
            },
            onFailure = { exception ->
                Log.e("ARSceneScreen", "Failed to host cloud anchor", exception)
                Toast.makeText(context, "Failed to host cloud anchor.", Toast.LENGTH_SHORT).show()
                hostingState = HostingState.PLACING
                onFailure(Exception(exception))
            }
        )
    }

    private fun hostCloudAnchor(anchor: com.google.ar.core.Anchor, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val session = session ?: return onFailure(Exception("Session is null"))
        session.hostCloudAnchorAsync(anchor,365) { hostedAnchor, cloudState ->
            if (cloudState == com.google.ar.core.Anchor.CloudAnchorState.SUCCESS) {
                onSuccess(hostedAnchor)
            } else {
                onFailure(Exception("Failed to host cloud anchor, state: $cloudState"))
            }
        }
    }


    /**
     * ARSCENE RESOLVING FUNCTIONS
     **/
    fun resolveCloudAnchors(anchorRoute: AnchorRoute?) {
        anchorRoute?.anchors?.forEach { anchor ->
            if (!resolvedAnchors.containsKey(anchor.id)) {
                resolvedAnchors[anchor.id] = Pair(anchor, false)
                resolveCloudAnchorById(
                    anchor.id,
                    onSuccess = { cloudAnchorNode ->
                        mainScope.launch {
                            handleResolvedAnchor(anchor, cloudAnchorNode)
                            resolvedAnchors[anchor.id] = Pair(anchor,true)
                        }
                    },
                    onFailure = { exception ->
                        Log.e("ARSceneScreen", "Failed to resolve cloud anchor with ID: ${anchor.id}", exception)
                    }
                )
            }
        }
    }


    private fun resolveCloudAnchorById(cloudAnchorId: String, onSuccess: (CloudAnchorNode) -> Unit, onFailure: (Exception) -> Unit) {
        CloudAnchorNode.resolve(engine = engine, session = session!!, cloudAnchorId = cloudAnchorId) { state, node ->
            when(state){
                com.google.ar.core.Anchor.CloudAnchorState.SUCCESS -> node?.let { onSuccess(it) }
                else -> onFailure(Exception("Failed to resolve cloud anchor with ID: $cloudAnchorId, state: ${state.name}"))
            }
        }
    }


    fun resolveEarthAnchor(earth: Earth, markerRoute: MarkerRoute?) {
        if(earth.trackingState ==TrackingState.TRACKING){
            markerRoute?.markers?.forEach { marker ->
                if (!resolvedMarkers.containsKey(marker.location)) {
                    resolvedMarkers[marker.location] = Pair(marker, false)
                    resolveEarthAnchorByLocation(
                        earth,
                        marker.location,
                        marker.altitude,
                        onSuccess = { earthAnchorNode ->
                            ioScope.launch {
                                handleResolvedMarker(marker, earthAnchorNode)
                                resolvedMarkers[marker.location] = Pair(marker, true)
                            }
                        },
                        onFailure = { exception ->
                            earth.createAnchor(
                                marker.location.latitude,
                                marker.location.longitude,
                                marker.altitude,
                                0f,0f,0f,0f
                            ).let {
                                ioScope.launch {
                                    handleResolvedMarker(marker, it)
                                    resolvedMarkers[marker.location] = Pair(marker, true)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun resolveEarthAnchorByLocation(earth: Earth, customLatLng: CustomLatLng, altitude: Double, onSuccess: (com.google.ar.core.Anchor) -> Unit, onFailure: (Exception) -> Unit) {
        earth.resolveAnchorOnTerrainAsync(
            customLatLng.latitude,
            customLatLng.longitude,
            0.0,0.0f,0.0f,0.0f,0.0f){ anchor,state ->
            when (state) {
                com.google.ar.core.Anchor.TerrainAnchorState.SUCCESS -> {
                    Log.d("EarthAnchor", "Terrain anchor resolved successfully")
                    onSuccess(anchor)
                }
                else -> {
                    Log.d("EarthAnchor", "Terrain anchor state: $state")
                    onFailure(Exception("Failed to resolve EarthAnchor, state: ${state.name}"))
                }
            }
        }
    }


    private suspend fun handleResolvedAnchor(anchor: Anchor, cloudAnchorNode: CloudAnchorNode) {
        cloudAnchorNode.let {
            childNodes.add(Pair(anchor.id, it))
            addModelToAnchorNode(it, anchor)
        }
    }

    private suspend fun handleResolvedMarker(marker: Marker, anchor: com.google.ar.core.Anchor?) {
        anchor.let {
            if (it != null) {
                createAnchorNodeFromAnchor(it).let {
                    childNodes.add(Pair(marker.name,it))
                    addModelToAnchorNode(it,marker)
                }

            }
        }
    }


    /**
     * ARSCENE CREATE ANCHORS
     **/
    fun createAnchorFromMotionEvent(motionEvent: MotionEvent):com.google.ar.core.Anchor? {
        val hitResult = frame?.hitTest(motionEvent)?.firstOrNull {
            val trackable = it.trackable
            trackable is DepthPoint || trackable is Plane || trackable is Point
        }

        return hitResult?.createAnchor()
    }

    fun createAnchorNodeFromAnchor(
        anchor: com.google.ar.core.Anchor,
        onTrackingStateChanged: ((TrackingState) -> Unit)? = null,
        onPoseChanged: ((Pose) -> Unit)? = null,
        onAnchorChanged: ((com.google.ar.core.Anchor) -> Unit)? = null,
        onUpdated: ((com.google.ar.core.Anchor) -> Unit)? = null
    ): AnchorNode {
        return AnchorNode(
            engine,
            anchor,
            onTrackingStateChanged = onTrackingStateChanged,
            onPoseChanged = onPoseChanged,
            onAnchorChanged = onAnchorChanged,
            onUpdated = onUpdated
        )
    }


    suspend fun addModelToAnchorNode(anchorNode: AnchorNode, anchor: Anchor){
        mainScope.launch {
            val modelNode = loadModelAndCreateNode(
                engine,
                anchorNode,
                modelLoader,
                assetModel = anchor.model,
                anchor = anchor
            )
            anchorNode.addChildNode(modelNode)
        }


    }

    suspend fun addModelToAnchorNode(anchorNode: AnchorNode, marker: Marker){
        ioScope.launch {
            val modelNode = loadModelAndCreateNode(
                engine,
                anchorNode,
                modelLoader,
                assetModel = marker.model
            )
            anchorNode.addChildNode(modelNode)
        }


    }
    private suspend fun loadModelAndCreateNode(engine: Engine, anchorNode: AnchorNode, modelLoader: ModelLoader,
                                               scale: Float = 1f, azimuth: Rotation = Rotation(0f, 0f, 0f), isSingleTapEnabled: Boolean = true,
                                               isDoubleTapEnabled: Boolean = true, assetModel: String, anchor: Anchor? = null
    ): ModelNode {
        val modelInstance = loadModelInstance(modelLoader, assetModel)
        val modelNode = createModelNode(modelInstance, anchorNode, scale, azimuth, isSingleTapEnabled, isDoubleTapEnabled)

        return modelNode
    }

    private suspend fun loadModelInstance(modelLoader: ModelLoader, assetModel: String): ModelInstance {
        isLoading = true
        var modelInstance: ModelInstance? = null
        while (modelInstance == null) {
            modelInstance = loadModelFromViewModel(assetModel,context, modelLoader)
        }
        isLoading = false
        return modelInstance
    }

    private fun createModelNode(modelInstance: ModelInstance?, anchorNode: AnchorNode,
                                scale: Float, azimuth: Rotation, isSingleTapEnabled: Boolean, isDoubleTapEnabled: Boolean): ModelNode {
        return ModelNode(
            modelInstance = modelInstance!!.apply {
                isShadowReceiver = false
                setScreenSpaceContactShadows(true)
            },
            centerOrigin = Position(x = 0f, y = 0f, z = 0f),
            autoAnimate = true,
            scaleToUnits = scale
        )

    }



    private suspend fun loadModelFromViewModel(
        assetModel: String,
        context: Context,
        modelLoader: ModelLoader
    ): ModelInstance? = withContext(Dispatchers.Main) {
        try {
            val modelData = arSceneViewModel.loadModelData(assetModel) { resourceFileName ->
                context.loadFileBuffer(resourceFileName)
            }
            return@withContext modelData?.let {
                modelLoader.createModelInstance(it.buffer)
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Error al crear la instancia del modelo")
            e.printStackTrace()
            return@withContext null
        }
    }




    suspend fun createAnchorNode(anchorNode: AnchorNode, ): AnchorNode = withContext(Dispatchers.IO){

        val cubeNode = CubeNode(
            engine,
            Size(0.2f,0.2f,0.2f),
            Cube.DEFAULT_CENTER,
            materialLoader.createColorInstance(Color.White.copy(alpha = 1f))).apply {
                withContext(Dispatchers.Main){
                }
        }
        anchorNode.clearChildNodes()
        anchorNode.addChildNode(cubeNode)
        return@withContext anchorNode
    }
}



private fun loadingAnimation(cubeNode: CubeNode) {

    val jumpAnimator = ValueAnimator.ofFloat(0f, 0.2f).apply {
        duration = 3000
        interpolator = AccelerateDecelerateInterpolator()
        repeatMode = ValueAnimator.REVERSE
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener { animation ->
            val translationY = animation.animatedValue as Float
            cubeNode.position = Position(y = translationY)
        }
    }
    val rotationAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 6000
        interpolator = AccelerateDecelerateInterpolator()
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        addUpdateListener { animation ->
            val newRotation = animation.animatedValue as Float
            cubeNode.rotation = Float3(0f, newRotation, 0f)
        }
    }
    jumpAnimator.start()
    rotationAnimator.start()
}





