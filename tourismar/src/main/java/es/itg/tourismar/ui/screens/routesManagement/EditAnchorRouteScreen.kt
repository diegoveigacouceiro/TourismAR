package es.itg.tourismar.ui.screens.routesManagement

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import es.itg.tourismar.data.model.anchor.AnchorRoute
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import es.itg.tourismar.R
import es.itg.tourismar.data.model.anchor.Anchor
import es.itg.tourismar.navigation.Screens
import es.itg.tourismar.ui.screens.markerScreen.MarkerRouteCard
import es.itg.tourismar.ui.theme.SceneViewTheme

@Composable
fun EditAnchorRouteScreen(
    anchorRoute: AnchorRoute?,
    navController: NavController,
    modifier: Modifier,
    viewModel: RoutesManagementViewModel = hiltViewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
//    var selectedRoute by remember { mutableStateOf<AnchorRoute?>(anchorRoute) }


    SceneViewTheme(
        dynamicColor = true
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()

        ) {
            anchorRoute?.let {
                EditAnchorRouteScreenContent(
                    it,
                    navController,
                    viewModel,
                    modifier,
                    selectedImageUri,
                    { uri -> selectedImageUri = uri }
                ) { showDeleteDialog = true }
            }

            if (showDeleteDialog) {
                ShowDeleteRouteConfirmationDialog(
                    anchorRoute!!.id,
                    navController,
                    viewModel
                ) { showDeleteDialog = false }
            }
        }
    }
}



@Composable
fun EditAnchorRouteScreenContent(
    anchorRoute: AnchorRoute,
    navController: NavController,
    viewModel: RoutesManagementViewModel,
    modifier: Modifier,
    selectedImageUri: Uri?,
    onImageSelected: (Uri) -> Unit,
    onClickDelete: () -> Unit
) {
    var selectedRoute by remember { mutableStateOf(anchorRoute) }
    val routeName = remember { mutableStateOf(selectedRoute.anchorRouteName) }
    val routeDescription = remember { mutableStateOf(selectedRoute.description) }
    val isExistingRoute = selectedRoute.id.isNotEmpty()
    var expanded by remember { mutableStateOf(false) }
    var selectedAnchor by remember { mutableStateOf<Anchor?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp), // Espacio para los botones flotantes
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isExistingRoute) {
                ElevatedButton(
                    onClick = onClickDelete,
                    modifier = modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    Text(text = stringResource(R.string.delete))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            ImagePicker(selectedImageUri, onImageSelected)

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = routeName.value,
                onValueChange = { routeName.value = it },
                label = { Text(stringResource(R.string.route_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = routeDescription.value,
                onValueChange = { routeDescription.value = it },
                label = { Text(stringResource(R.string.route_description)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .height(LocalConfiguration.current.screenHeightDp.dp)
            ) {
                items(selectedRoute.anchors) { anchor ->
                    AnchorItem(anchor = anchor,
                        isSelected = anchor == selectedAnchor,
                        onClick = {
                        selectedAnchor = if (selectedAnchor == anchor) null else anchor
                    } ,
                        onDelete = {selectedRoute.anchors.toHashSet().remove(anchor)}
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    if (isExistingRoute) {
                        viewModel.updateAnchorRoute(
                            selectedRoute.copy(
                                anchorRouteName = routeName.value,
                                description = routeDescription.value
                            )
                        )
                    } else {
                        viewModel.createAnchorRoute(
                            selectedRoute.copy(
                                anchorRouteName = routeName.value,
                                description = routeDescription.value
                            )
                        )
                    }
                    navController.popBackStack()
                }
            ) {
                Icon(Icons.Filled.Done, contentDescription = stringResource(R.string.save))
            }

            FloatingActionButton(
                onClick = {
                    navController.currentBackStackEntry?.savedStateHandle?.set("anchorRoute", selectedRoute)
                    navController.navigate(Screens.ARScene.route)
                }
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.ar_scene))
            }
        }
    }
}

@Composable
fun AnchorItem(anchor: Anchor, isSelected: Boolean, onClick: () -> Unit, onDelete: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .padding(8.dp)
    ) {
        Text(text = anchor.name, style = MaterialTheme.typography.bodyLarge)

        if (isSelected) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "ID: ${anchor.id}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Model: ${anchor.model}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Order: ${anchor.order}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Serialized Time: ${anchor.serializedTime}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Location: (${anchor.location.latitude}, ${anchor.location.longitude})", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Pose: ${anchor.pose}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "API Link: ${anchor.apiLink}", style = MaterialTheme.typography.bodyMedium)
            IconButton(onClick = { onDelete() }) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
    }
}




@Composable
fun ImagePicker(
    selectedImageUri: Uri?,
    onImageSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onBackground),
        contentAlignment = Alignment.Center
    ) {
        if (selectedImageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(selectedImageUri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            IconButton(
                onClick = { launcher.launch("image/*") }
            ) {
                Icon(
                    Icons.TwoTone.Add,
                    contentDescription = stringResource(R.string.add_photo),
                    modifier = Modifier.size(48.dp),
                    tint =MaterialTheme.colorScheme.background
                    )
            }
        }
    }
}


@Composable
fun ShowDeleteRouteConfirmationDialog(
    routeId: String,
    navController: NavController,
    viewModel: RoutesManagementViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_route_confirmation)) },
        text = { Text(stringResource(R.string.delete_route_message)) },
        confirmButton = {
            TextButton(onClick = {
                viewModel.deleteAnchorRoute(routeId)
                navController.popBackStack()
            }) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}


