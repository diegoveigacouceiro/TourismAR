package es.itg.tourismar.ui.screens.arscreen

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.filament.Engine
import com.google.android.filament.View
import com.google.ar.core.Frame
import com.google.ar.core.ResolveCloudAnchorFuture
import com.google.ar.core.Session
import com.google.ar.core.TrackingFailureReason
import es.itg.tourismar.data.model.anchor.Anchor
import io.github.sceneview.ar.camera.ARCameraStream
import io.github.sceneview.ar.node.ARCameraNode
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.node.CloudAnchorNode
import io.github.sceneview.collision.CollisionSystem
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



    fun getmodel(){
        arSceneViewModel.getdata()
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
//
//    suspend fun createAnchorNode(engine: Engine, modelLoader: ModelLoader, anchorNode: AnchorNode,
//                                 isPositionEditable: Boolean = false, isSingleTapEnabled: Boolean = true,
//                                 isDoubleTapEnabled: Boolean = true,
//                                 anchor: Anchor? = null
//    ): AnchorNode = withContext(Dispatchers.IO)    {
//
//
//        val modelNode = loadModelAndCreateNode(
//            engine,
//            anchorNode,
//            modelLoader,
//            isSingleTapEnabled = isSingleTapEnabled,
//            isDoubleTapEnabled = isDoubleTapEnabled,
//            assetModel = anchor?.model,
//            anchor = anchor
//        )
//        setAnchorNodeEditability(anchorNode,
//            isPositionEditable = isPositionEditable)
//
//        anchorNode.clearChildNodes()
//        anchorNode.addChildNode(modelNode)
//        return@withContext anchorNode
//    }
//
//    private suspend fun loadModelAndCreateNode(engine: Engine, anchorNode: AnchorNode, modelLoader: ModelLoader,
//                                               scale: Float = 1.5f, azimuth: Rotation = Rotation(0f, 0f, 0f), isSingleTapEnabled: Boolean = true,
//                                               isDoubleTapEnabled: Boolean = true, assetModel: String, anchor: Anchor? = null
//    ): ModelNode {
//        val modelInstance = loadModelInstance(modelLoader, assetModel)
//        val modelNode = createModelNode(modelInstance, anchorNode, scale, azimuth, isSingleTapEnabled, isDoubleTapEnabled)
//
//
//
//        return modelNode
//    }
//
//    private suspend fun loadModelInstance(modelLoader: ModelLoader, assetModel: String): ModelInstance {
//        isLoading = true
//        var modelInstance: ModelInstance? = null
//        while (modelInstance == null) {
//            modelInstance = loadModelFromStorage(context, cloudStorageHandler, modelLoader, assetModel)
//        }
//        isLoading = false
//        return modelInstance
//    }
//
//    private fun createModelNode(modelInstance: ModelInstance?, anchorNode: AnchorNode,
//                                scale: Float, azimuth: Rotation, isSingleTapEnabled: Boolean, isDoubleTapEnabled: Boolean): ModelNode {
//        return ModelNode(
//            modelInstance = modelInstance!!.apply {
//                isShadowReceiver = false
//                setScreenSpaceContactShadows(true)
//            },
//            centerOrigin = Position(x = 0f, y = 0f, z = 0f),
//            autoAnimate = true,
//            scaleToUnits = scale
//        )
//
//    }
//
//    private suspend fun loadModelFromStorage(
//        context: Context,
//        cloudStorageHandler: CloudStorageHandler,
//        modelLoader: ModelLoader,
//        assetModel: String
//    ): ModelInstance? = withContext(Dispatchers.Main) {
//        var model = modelName
//
//        if (!assetModel.isNullOrEmpty()){
//            model = assetModel
//        }
//        try {
//            val modelData = cloudStorageHandler.loadModelDataFromStorage(model) { resourceFileName ->
//                context.loadFileBuffer(resourceFileName)
//            }
//            return@withContext modelData?.let {
//                modelLoader.createModelInstance(it.buffer)
//            }
//        } catch (e: Exception) {
//            Log.e("Firebase", "Error al crear la instancia del modelo")
//            e.printStackTrace()
//            return@withContext null
//        }
//    }
//
//
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
//
//    fun handleCloudAnchors(anchor:Anchor) {
//
//    }


}

