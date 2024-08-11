package es.itg.tourismar.ui.screens.routesManagement

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import es.itg.tourismar.R
import es.itg.tourismar.data.model.anchor.Anchor
import es.itg.tourismar.data.model.users.UserLevel
import es.itg.tourismar.navigation.Screens
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
    var selectedImageName by remember { mutableStateOf(anchorRoute?.imageUrl) }

    LaunchedEffect(anchorRoute) {
        if (anchorRoute != null) {
            viewModel.setSelectedRoute(anchorRoute)
            selectedImageName = anchorRoute.imageUrl
            if (!selectedImageName.isNullOrEmpty()) {
                viewModel.getImage(selectedImageName!!)
            }
        }
    }

    val imageUri by viewModel.selectedImageUri.observeAsState()

    LaunchedEffect(imageUri) {
        if (imageUri != null) {
            selectedImageUri = imageUri
        }
    }

    SceneViewTheme(
        dynamicColor = true
    ) {
        Surface(
            modifier = modifier.fillMaxSize()
        ) {
            anchorRoute?.let {
                EditAnchorRouteScreenContent(
                    it,
                    navController,
                    viewModel,
                    modifier,
                    selectedImageUri,
                    selectedImageName,
                    { uri, name ->
                        selectedImageUri = uri
                        selectedImageName = name
                    }
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
    selectedImageName: String?,
    onImageSelected: (Uri, String) -> Unit,
    onClickDelete: () -> Unit
) {
    val selectedRoute by viewModel.selectedRoute.observeAsState()
    if (selectedRoute == null) return

    val routeName = remember { mutableStateOf(selectedRoute!!.anchorRouteName) }
    val routeDescription = remember { mutableStateOf(selectedRoute!!.description) }
    val isExistingRoute = selectedRoute!!.id.isNotEmpty()
    var selectedAnchors by remember { mutableStateOf(setOf<Anchor>()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedButton(
                onClick = onClickDelete,
                enabled = isExistingRoute,
                modifier = modifier
                    .padding(16.dp)
                    .align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Text(text = stringResource(R.string.delete))
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .offset(y = (60).dp),
            ){
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = routeName.value,
                        onValueChange = { routeName.value = it },
                        label = { Text(stringResource(R.string.route_name)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    TextField(
                        value = routeDescription.value,
                        onValueChange = { routeDescription.value = it },
                        label = { Text(stringResource(R.string.route_description)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        items(selectedRoute!!.anchors) { anchor ->
                            AnchorItem(
                                anchor = anchor,
                                isSelected = selectedAnchors.contains(anchor),
                                onClick = {
                                    selectedAnchors = if (selectedAnchors.contains(anchor)) {
                                        selectedAnchors - anchor
                                    } else {
                                        selectedAnchors + anchor
                                    }
                                },
                                onDelete = {
                                    val updatedAnchors = selectedRoute!!.anchors.filter { it.id != anchor.id }
                                    viewModel.setSelectedRoute(selectedRoute!!.copy(anchors = updatedAnchors))
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    if (isExistingRoute) {
                        if (selectedImageUri != null && selectedImageName != null) {
                            viewModel.saveImage(selectedImageUri, selectedImageName)
                        }
                        viewModel.updateAnchorRoute(
                            selectedRoute!!.copy(
                                anchorRouteName = routeName.value,
                                description = routeDescription.value,
                                imageUrl = selectedImageName ?: ""
                            )
                        )
                    } else {
                        viewModel.createAnchorRoute(
                            selectedRoute!!.copy(
                                anchorRouteName = routeName.value,
                                description = routeDescription.value,
                                imageUrl = selectedImageName ?: ""
                            )
                        )
                    }
                }
            ) {
                Icon(Icons.Filled.Done, contentDescription = stringResource(R.string.save))
            }

            FloatingActionButton(
                onClick = {
                    navController.currentBackStackEntry?.savedStateHandle?.set("anchorRoute", selectedRoute)
                    navController.currentBackStackEntry?.savedStateHandle?.set("userLevel", UserLevel.PRIVILEGED)
                    navController.navigate(Screens.ARScene.route)
                }
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.ar_scene))
            }
        }

        ImagePicker(selectedImageUri, onImageSelected)
    }
}


@Composable
fun AnchorItem(anchor: Anchor, isSelected: Boolean, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
    ){
        Column(modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = anchor.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontStyle = FontStyle.Italic
                )

                Icon(
                    imageVector = if (isSelected) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isSelected) "Collapse" else "Expand",
                )
            }


            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ID: ${anchor.id}",
                    style = MaterialTheme.typography.bodyMedium

                )
                Text(text = "Model: ${anchor.model}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Order: ${anchor.order}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Serialized Time: ${anchor.serializedTime}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Location: (${anchor.location.latitude}, ${anchor.location.longitude})", style = MaterialTheme.typography.bodyMedium)
                Text(text = "API Link: ${anchor.apiLink}", style = MaterialTheme.typography.bodyMedium)
                Button(
                    modifier = Modifier
                        .align(Alignment.End)
                        .align(AbsoluteAlignment.Right),
                    onClick = { onDelete() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = stringResource(R.string.delete))
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )

                }

            }
        }
    }

}



@Composable
fun ImagePicker(
    selectedImageUri: Uri?,
    onImageSelected: (Uri, String) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileNameFromUri(context, it)
            onImageSelected(it, fileName)
        }
    }

    if (selectedImageUri != null) {
        Image(
            painter = rememberAsyncImagePainter(selectedImageUri),
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .offset(x = 120.dp, y = 40.dp)
                .fillMaxSize()
                .clip(CircleShape)
                .clickable {
                    launcher.launch("image/*")
                },
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )
    } else {
        IconButton(
            modifier = Modifier
                .size(150.dp)
                .offset(x = 120.dp, y = 40.dp)
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimaryContainer),
            onClick = { launcher.launch("image/*") }
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = stringResource(R.string.add_photo),
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

}

fun getFileNameFromUri(context: Context, uri: Uri): String {
    var fileName = ""
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                fileName = cursor.getString(nameIndex)
            }
        }
    }
    return fileName
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
                navController.navigate(Screens.RoutesManagement.route)
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


