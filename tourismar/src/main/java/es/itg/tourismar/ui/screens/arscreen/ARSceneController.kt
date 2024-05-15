package es.itg.tourismar.ui.screens.arscreen

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.android.filament.Engine
import com.google.android.filament.View
import com.google.ar.core.DepthPoint
import com.google.ar.core.Frame
import com.google.ar.core.Point
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import es.itg.tourismar.data.model.anchor.Anchor
import io.github.sceneview.ar.camera.ARCameraStream
import io.github.sceneview.ar.node.ARCameraNode
import io.github.sceneview.ar.node.AnchorNode
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
import kotlinx.coroutines.withContext


class ARSceneController(
    val engine: Engine,
    val modelLoader: ModelLoader,
    val materialLoader: MaterialLoader,
    val cameraNode: ARCameraNode,
    val cameraStream: ARCameraStream,
    var view: View,
    val collisionSystem: CollisionSystem,
    private val context: Context,
    val mainLightNode: LightNode,
    val arSceneViewModel: ARSceneViewModel
) {
    val childNodes = SnapshotStateList<Pair<String?, Node>>()
    var planeRenderer by mutableStateOf(false)
    var trackingFailureReason by mutableStateOf<TrackingFailureReason?>(null)
    var frame by mutableStateOf<Frame?>(null)
    var session by mutableStateOf<Session?>(null)
    var isLoading by mutableStateOf(false)
    val viewModelScope = CoroutineScope(Dispatchers.Main)




    fun createAnchorFromMotionEvent(
        motionEvent: MotionEvent
    ):com.google.ar.core.Anchor? {

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


//    fun resolveCloudAnchor(anchor: Anchor):ResolveCloudAnchorFuture{
//        return CloudAnchorNode.resolve(
//                engine = engine,
//                session = session!!,
//                cloudAnchorId = anchor.id
//            ) { state, node ->
//                if (!state.isError && node != null) {
//                    createAnchorNode(
//                        engine = engine,
//                        modelLoader = modelLoader,
//                        anchorNode = node,
//                        isSingleTapEnabled = false,
//                        isDoubleTapEnabled = false,
//                        isPositionEditable = false,
//                        anchor = anchor
//                    )
//
//                }
//            }
//        }

    suspend fun createAnchorNode(anchorNode: AnchorNode,
                                 isPositionEditable: Boolean = false, isSingleTapEnabled: Boolean = true,
                                 isDoubleTapEnabled: Boolean = true,
                                 anchor: Anchor? = null
    ): AnchorNode = withContext(Dispatchers.IO){


        val modelNode = anchor?.model.let {
            loadModelAndCreateNode(
                engine,
                anchorNode,
                modelLoader,
                isSingleTapEnabled = isSingleTapEnabled,
                isDoubleTapEnabled = isDoubleTapEnabled,
                assetModel = "eiffel_tower.glb",
                anchor = anchor
            )
        }
//        setAnchorNodeEditability(anchorNode,
//            isPositionEditable = isPositionEditable)

        anchorNode.clearChildNodes()
        anchorNode.addChildNode(modelNode)
        return@withContext anchorNode
    }

    private suspend fun loadModelAndCreateNode(engine: Engine, anchorNode: AnchorNode, modelLoader: ModelLoader,
                                               scale: Float = 1.5f, azimuth: Rotation = Rotation(0f, 0f, 0f), isSingleTapEnabled: Boolean = true,
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




    fun handleCloudAnchors(anchor:Anchor) {

    }
}




//    suspend fun getModelsName(): List<String> = withContext(Dispatchers.Default){
//
//        try {
//            return@withContext cloudStorageHandler.getFileNames()
//        }catch (e: Exception){
//            Log.e("Firebase","error al obtener los nombres")
//            e.printStackTrace()
//            return@withContext emptyList()
//        }
//
//    }






