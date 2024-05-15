package es.itg.tourismar.ui.screens.arscreen


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.LocationOn
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.ar.core.Config
import com.google.ar.core.Plane
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.ui.screens.googleMap.MapComposable
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.getDescription
import io.github.sceneview.rememberOnGestureListener
import kotlinx.coroutines.launch

private const val kModelFile = "models/damaged_helmet.glb"
private const val kMaxModelInstances = 10


@Composable
fun ARSceneScreen(navController: NavController, anchorRoute: AnchorRoute?, viewModel: ARSceneViewModel = hiltViewModel()) {
    val arSceneController = ARSceneControllerFactory.create(viewModel)

    Scaffold(
        floatingActionButton = {
            ARSceneFloatingActions(
                navController = navController,
                anchorRoute,
                arSceneController
            )
       },
        content = { paddingValues ->
            ARSceneScreenContent(
                navController = navController,
                anchorRoute = anchorRoute,
                arSceneController = arSceneController,
                paddingValues = paddingValues
            )
        }
    )
}
@Composable
fun ARSceneScreenContent(navController: NavController, anchorRoute: AnchorRoute?,
                         arSceneController: ARSceneController,paddingValues: PaddingValues
) {
    Surface (
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        color = Color.Black
    ) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            childNodes = arSceneController.childNodes.map { it.second },
            engine = arSceneController.engine,
            view = arSceneController.view,
            modelLoader = arSceneController.modelLoader,
            collisionSystem = arSceneController.collisionSystem,
            sessionCameraConfig = {
                it.cameraConfig
            },
            mainLightNode = arSceneController.mainLightNode,
            sessionConfiguration = { session, config ->
                // Activation Depth occlusion
                config.depthMode =
                    when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                        true -> Config.DepthMode.AUTOMATIC
                        false -> Config.DepthMode.DISABLED
                    }
                arSceneController.cameraStream.isDepthOcclusionEnabled = false

                // Activación Geospatial
                if (session.isGeospatialModeSupported(Config.GeospatialMode.ENABLED))
                    config.geospatialMode = Config.GeospatialMode.ENABLED
                config.streetscapeGeometryMode = Config.StreetscapeGeometryMode.ENABLED

                // Activación CloudAnchors
                config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED

                // Configuración luz
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR

                config.focusMode = Config.FocusMode.AUTO

                config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                //config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                arSceneController.cameraNode.focalLength = 50.0
            },
            cameraNode = arSceneController.cameraNode,
            cameraStream = arSceneController.cameraStream,
            planeRenderer = arSceneController.planeRenderer,
            onTrackingFailureChanged = { arSceneController.trackingFailureReason = it },
            onSessionUpdated = { session, updatedFrame ->
                arSceneController.frame = updatedFrame
                arSceneController.session = session
//                arSceneController.handleCloudAnchors(anchorRoute)
                if (arSceneController.childNodes.isEmpty()) {
                    updatedFrame.getUpdatedPlanes()
                        .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                        ?.let { it.createAnchorOrNull(it.centerPose) }?.let { anchor ->
                            arSceneController.createAnchorNodeFromAnchor(anchor).let {
                                arSceneController.viewModelScope.launch {
                                    with(arSceneController) {
                                        childNodes.add(Pair(null, it))
                                        createAnchorNode(anchorNode = it)
                                    }
                                }
                            }
                        }
                }

            },

            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { motionEvent, node ->
                   arSceneController.createAnchorFromMotionEvent(motionEvent).let { anchor ->
                       if (anchor != null) {
                           arSceneController.createAnchorNodeFromAnchor(anchor).let {
                               arSceneController.viewModelScope.launch {
                                   with(arSceneController) {
                                       childNodes.add(Pair(null, it))
                                       createAnchorNode(anchorNode = it)
                                   }
                               }
                           }
                       }

                   }
                },

            )
        )

        Text(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            color = Color.White,
            text = arSceneController.trackingFailureReason?.getDescription(LocalContext.current)
                ?: ""
        )
    }
}

@Composable
fun ARSceneFloatingActions(navController: NavController,anchorRoute: AnchorRoute?, viewController: ARSceneController) {
    Column {
        ARSceneFloatingActionButton(
            onClick = {
                ShowARSceneOptionsDialog(viewController)
            },
            icon = Icons.TwoTone.Settings,
            contentDescription = "Properties",
            modifier = Modifier.padding(8.dp)
        )

        ARSceneFloatingActionButton(
            onClick = {
                MapComposable(anchorRoute= anchorRoute)
            },
            icon = Icons.TwoTone.LocationOn,
            contentDescription = "Map",
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun ARSceneFloatingActionButton(
    onClick: @Composable () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val showDialog = remember { mutableStateOf(false) }

    FloatingActionButton(
        onClick = { showDialog.value = true },
        modifier = modifier
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }

    if (showDialog.value) {
        Dialog(
            onDismissRequest = { showDialog.value = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                securePolicy = SecureFlagPolicy.SecureOn
            )
        ){
            ElevatedCard(
                modifier = Modifier
                    .padding(16.dp)
                    .size(600.dp),
                shape = ShapeDefaults.Medium,
                elevation =  CardDefaults.elevatedCardElevation(),
                colors = CardDefaults.elevatedCardColors(),
            ) {
                onClick()
            }
        }
    }
}

@Composable
fun ShowARSceneOptionsDialog(viewController: ARSceneController) {
    // Implementa el AlertDialog con las opciones del ARScene
}



