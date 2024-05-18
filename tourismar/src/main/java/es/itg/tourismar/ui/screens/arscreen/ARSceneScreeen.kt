package es.itg.tourismar.ui.screens.arscreen


import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material.icons.twotone.LocationOn
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingFailureReason
import es.itg.tourismar.data.model.anchor.Anchor
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.data.model.anchor.CustomLatLng
import es.itg.tourismar.data.model.anchor.HostingState
import es.itg.tourismar.data.model.anchor.Pose
import es.itg.tourismar.data.model.anchor.ScanningState
import es.itg.tourismar.data.model.anchor.SerializableFloat3
import es.itg.tourismar.ui.screens.arscreen.controllers.ARSceneController
import es.itg.tourismar.ui.screens.arscreen.controllers.ARSceneControllerFactory
import es.itg.tourismar.ui.screens.googleMap.MapComposable
import es.itg.tourismar.util.Resource
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.ar.rememberARCameraStream
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val kModelFile = "models/damaged_helmet.glb"
private const val kMaxModelInstances = 10


@Composable
fun ARSceneScreen(navController: NavController, anchorRoute: AnchorRoute?, viewModel: ARSceneViewModel = hiltViewModel()) {

    val trackingFailureReason by remember { mutableStateOf<TrackingFailureReason?>(null) }
    val frame by remember { mutableStateOf<Frame?>(null) }
    val session by remember { mutableStateOf<Session?>(null) }
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine = engine)
    val materialLoader = rememberMaterialLoader(engine)
    val cameraNode = rememberARCameraNode(engine)
    val cameraStream = rememberARCameraStream(materialLoader = materialLoader)
    val view = rememberView(engine)
    val collisionSystem = rememberCollisionSystem(view = view)
    val context = LocalContext.current
    val mainLightNode = rememberMainLightNode(engine = engine)
    val arSceneController = remember {
        ARSceneControllerFactory.create(engine,modelLoader,materialLoader,cameraNode,
        cameraStream,view,collisionSystem,context,mainLightNode,viewModel,frame,session,trackingFailureReason )}


    Scaffold(
        floatingActionButton = {
            ARSceneFloatingActions(
                navController = navController,
                anchorRoute,
                arSceneController,
                viewModel
            )
        }
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
                with(arSceneController){
                    this.frame = updatedFrame
                    this.session = session
                    updateScanningState(updatedFrame)
                    resolveCloudAnchors(anchorRoute)
                }
            },

            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { motionEvent, node ->
                    if (arSceneController.hostingState == HostingState.PLACING) {
                        arSceneController.createAnchorFromMotionEvent(motionEvent)?.let { anchor ->
                            arSceneController.createAnchorNodeFromAnchor(anchor).let {
                                arSceneController.viewModelScope.launch {
                                    with(arSceneController) {
                                        childNodes.add(Pair(null, it))
                                        createAnchorNode(anchorNode = it)
                                    }
                                    with(arSceneController){
                                        placedAnchor = anchor
                                        hostingState = HostingState.READY_TO_HOST
                                        scanningMessage = "Anchor placed. Tap the upload button to host it."
                                    }
                                }
                            }
                        }
                    }
                },
            )
        )

        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = Color.White,
                text = arSceneController.trackingFailureReason?.getDescription(LocalContext.current)
                    ?: ""
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = Color.White,
                text = arSceneController.scanningMessage
            )
        }
    }
}



@Composable
fun ARSceneFloatingActions(navController: NavController, anchorRoute: AnchorRoute?, viewController: ARSceneController, viewModel: ARSceneViewModel) {
    val showDialogProperties = remember { mutableStateOf(false) }
    val showDialogMap = remember { mutableStateOf(false) }
    val showDialogAnchorForm = remember { mutableStateOf(false) }
    val cloudAnchorId = remember { mutableStateOf("") }

    Column {
        ARSceneFloatingActionButton(
            onClick = { showDialogProperties.value = true },
            content = {
                ShowARSceneOptionsDialog(viewController)
            },
            icon = Icons.TwoTone.Settings,
            showDialog = showDialogProperties,
            contentDescription = "Properties",
            modifier = Modifier.padding(8.dp)
        )

        ARSceneFloatingActionButton(
            onClick = { showDialogMap.value = true },
            content = {
                MapComposable(anchorRoute = anchorRoute)
            },
            icon = Icons.TwoTone.LocationOn,
            showDialog = showDialogMap,
            contentDescription = "Map",
            modifier = Modifier.padding(8.dp)
        )

        ARSceneFloatingActionButton(
            onClick = {
                if (viewController.scanningState == ScanningState.READY_TO_HOST) {
                    viewController.placedAnchor?.let {
                        viewController.handleHosting(it,
                            onSuccess = { anchorId ->
                                cloudAnchorId.value = anchorId
                                showDialogAnchorForm.value = true
                            },
                            onFailure = {
                                viewController.placedAnchor?.detach()
                                viewController.placedAnchor = null
                            })
                        Toast.makeText(viewController.context, "Trying to host an anchor", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(viewController.context, "Keep scanning the environment before hosting an anchor.", Toast.LENGTH_SHORT).show()
                }
            },
            content = {
                AnchorForm(
                    viewModel = viewModel,
                    anchorId = cloudAnchorId.value,
                    anchorRoute = anchorRoute!!,
                    onSubmit = {
                        // Handle form submission
                    }
                )
            },
            icon = Icons.TwoTone.Add,
            showDialog = showDialogAnchorForm,
            contentDescription = "Host Anchor",
            modifier = Modifier.padding(8.dp),
            containerColor = if (viewController.scanningState == ScanningState.READY_TO_HOST) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun ARSceneFloatingActionButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
    showDialog: MutableState<Boolean>,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor
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
        ) {
            ElevatedCard(
                modifier = Modifier
                    .padding(16.dp)
                    .size(600.dp),
                shape = ShapeDefaults.Medium,
                elevation = CardDefaults.elevatedCardElevation(),
                colors = CardDefaults.elevatedCardColors(),
            ) {
                content()
            }
        }
    }
}


@Composable
fun ShowARSceneOptionsDialog(viewController: ARSceneController) {
    // Implementa el AlertDialog con las opciones del ARScene
}

@Composable
fun AnchorForm(
    viewModel: ARSceneViewModel,
    anchorId: String,
    anchorRoute: AnchorRoute,
    onSubmit: (Anchor) -> Unit
) {
    val modelsResource by viewModel.models.collectAsState()
    var selectedModel by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val order = anchorRoute.anchors.size + 1
    val serializedTime = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()) }
    val location = CustomLatLng(
        latitude = 37.7749, // Example latitude, replace with actual location data
        longitude = -122.4194 // Example longitude, replace with actual location data
    )
    val pose = Pose(
        rotation = SerializableFloat3(0f, 0f, 0f),
        translation = SerializableFloat3(0f, 0f, 0f)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Create Anchor", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "ID: $anchorId")

        Spacer(modifier = Modifier.height(8.dp))

        when (modelsResource) {
            is Resource.Loading -> {
                CircularProgressIndicator()
            }
            is Resource.Success -> {
                val models = (modelsResource as Resource.Success<List<String>>).data

                if (models != null) {
                    if (models.isNotEmpty()) {
                        Box {
                            Text(selectedModel.ifEmpty { "Select a model" }, modifier = Modifier
                                .fillMaxWidth()
                                .clickable { dropdownExpanded = true })
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                models.forEach { model ->
                                    DropdownMenuItem(
                                        text= { Text(text = model) },
                                        onClick = {
                                        selectedModel = model
                                        dropdownExpanded = false
                                    })
                                }
                            }
                        }
                    } else {
                        Text(text = "No models available")
                    }
                }
            }
            is Resource.Error -> {
                val errorMessage = (modelsResource as Resource.Error).message
                Text(text = "Error: $errorMessage")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Order: $order")

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Serialized Time: $serializedTime")

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Location: (${location.latitude}, ${location.longitude})")

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Pose: Rotation (${pose.rotation.x}, ${pose.rotation.y}, ${pose.rotation.z}), Translation (${pose.translation.x}, ${pose.translation.y}, ${pose.translation.z})")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val newAnchor = Anchor(
                id = anchorId,
                model = selectedModel,
                name = name,
                order = order,
                serializedTime = serializedTime,
                location = location,
                pose = pose,
                apiLink = ""
            )
            onSubmit(newAnchor)
        }) {
            Text("Submit")
        }
    }
}




