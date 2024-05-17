package es.itg.tourismar.ui.screens.arscreen.controllers

import android.content.Context
import com.google.android.filament.Engine
import com.google.android.filament.View
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingFailureReason
import es.itg.tourismar.ui.screens.arscreen.ARSceneViewModel
import io.github.sceneview.ar.camera.ARCameraStream
import io.github.sceneview.ar.node.ARCameraNode
import io.github.sceneview.collision.CollisionSystem
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.node.LightNode

object ARSceneControllerFactory {
    fun create(
        engine: Engine,
        modelLoader: ModelLoader,
        materialLoader: MaterialLoader,
        cameraNode: ARCameraNode,
        cameraStream: ARCameraStream,
        view: View,
        collisionSystem: CollisionSystem,
        context: Context,
        mainLightNode: LightNode,
        arSceneViewModel: ARSceneViewModel,
        frame: Frame?,
        session: Session?,
        trackingFailureReason: TrackingFailureReason?
    ): ARSceneController {


        return ARSceneController(
            engine, modelLoader, materialLoader, cameraNode, cameraStream, view,
            collisionSystem, context, mainLightNode,arSceneViewModel, frame,session,trackingFailureReason
        )
    }
}


