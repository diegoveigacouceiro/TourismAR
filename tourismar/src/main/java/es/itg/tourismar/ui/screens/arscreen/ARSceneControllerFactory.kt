package es.itg.tourismar.ui.screens.arscreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.ar.rememberARCameraStream
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberView

object ARSceneControllerFactory {

    @Composable
    fun create(arSceneViewModel: ARSceneViewModel):ARSceneController{
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine = engine)
        val materialLoader = rememberMaterialLoader(engine)
        val cameraNode = rememberARCameraNode(engine)
        val cameraStream = rememberARCameraStream(materialLoader = materialLoader)
        val view = rememberView(engine)
        val collisionSystem = rememberCollisionSystem(view = view)
        val context = LocalContext.current
        val mainLightNode = rememberMainLightNode(engine = engine)


        return ARSceneController(
            engine, modelLoader, materialLoader, cameraNode, cameraStream, view,
            collisionSystem, context, mainLightNode, arSceneViewModel
        )
    }
}


