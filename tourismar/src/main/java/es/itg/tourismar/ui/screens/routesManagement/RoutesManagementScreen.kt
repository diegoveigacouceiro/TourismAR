package es.itg.tourismar.ui.screens.routesManagement

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment.Vertical
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import es.itg.tourismar.R
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.navigation.Screens
import es.itg.tourismar.ui.screens.googleMap.MapComposable
import es.itg.tourismar.ui.screens.home.RequestMultiplePermissionsComposable
import androidx.compose.ui.Alignment


@Composable
fun RoutesManagementScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: RoutesManagementViewModel = hiltViewModel()
) {
    val (showCreateRouteDialog, setShowCreateRouteDialog) = remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (!showCreateRouteDialog) {
                FloatingActionButton(
                    onClick = { setShowCreateRouteDialog(true) },
                    modifier = Modifier
                        .padding(bottom = 50.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_route))
                }
            }
        },
        content = { innerModifier ->
            innerModifier
            RoutesManagementContent(
                viewModel = viewModel,
                modifier = Modifier,
                navController = navController,
                onCreateRouteClicked = { setShowCreateRouteDialog(true) }
            )
            if (showCreateRouteDialog) {
                CreateRouteAlertDialog(
                    onDismissRequest = { setShowCreateRouteDialog(false) },
                    onCreateRoute = {  }
                )
            }
        }
    )
}

@Composable
fun RoutesManagementContent(
    viewModel: RoutesManagementViewModel,
    modifier: Modifier,
    navController: NavController,
    onCreateRouteClicked: () -> Unit
) {
    var anchorRoutesState by remember { mutableStateOf<List<AnchorRoute>?>(null) }
    var selectedAnchorRoute by remember { mutableStateOf<AnchorRoute?>(null)}

        LaunchedEffect(viewModel) {
            viewModel.anchorRoutes.observeForever { anchorRoutes ->
                anchorRoutesState = anchorRoutes
            }
        }

        Box(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize(1f)
        ) {
            Column {
                Spacer(modifier = Modifier.height(20.dp))

                // Existing Routes
                if (anchorRoutesState != null) {
                    Text(
                        text = stringResource(R.string.existing_routes),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    AnchorRoutesGrid(
                        anchorRoutes = anchorRoutesState!!,
                        modifier = Modifier.padding(vertical = 8.dp),
                        navController = navController
                    ) { anchorRoute ->
                        selectedAnchorRoute = anchorRoute
                        navController.currentBackStackEntry?.savedStateHandle?.set("anchorRoute", anchorRoute
                        )
                        navController.navigate(Screens.EditAnchorRoute.route)
                    }
                }
            }

            // AR Scene access for selected route (optional)
            selectedAnchorRoute?.let { anchorRoute ->
                FloatingActionButton(
                    onClick = {
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            "anchorRoute", anchorRoute
                        )
                        navController.navigate(Screens.ARScene.route)
                    },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.ar_scene)
                    )
                }
            }
        }
    }


@Composable
fun AnchorRoutesGrid(
    anchorRoutes: List<AnchorRoute>,
    modifier: Modifier = Modifier,
    navController: NavController,
    onAnchorRouteClicked: (AnchorRoute)-> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .height(LocalConfiguration.current.screenHeightDp.dp)
    ) {
        items(anchorRoutes) { anchorRoute ->
            AnchorRouteCard(anchorRoute = anchorRoute){
                onAnchorRouteClicked(anchorRoute)            }
        }
    }
}


@OptIn(ExperimentalCoilApi::class)
@Composable
fun AnchorRouteCard(
    anchorRoute: AnchorRoute,
    modifier: Modifier = Modifier,
    onClick: ()-> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = rememberImagePainter(R.mipmap.ic_torre_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(80.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .heightIn(max = 80.dp)
                        .clip(MaterialTheme.shapes.medium),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = anchorRoute.anchorRouteName!!,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = anchorRoute.description!!,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.width(200.dp),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun DetailedAnchorRouteCard(
    anchorRoute: AnchorRoute,
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = anchorRoute.anchorRouteName!!,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Description: ${anchorRoute.description}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Location: ${anchorRoute.anchors[0].location}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            RequestMultiplePermissionsComposable(permissions = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )) {
                ElevatedCard(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(500.dp),
                    shape = ShapeDefaults.Medium,
                    elevation =  CardDefaults.elevatedCardElevation(),
                    colors = CardDefaults.elevatedCardColors(),
                ) {
                    MapComposable(anchorRoute = anchorRoute)
                }
            }
            ElevatedButton(
                onClick = onBackClicked,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = "AR")
            }
        }
    }
}

@Composable
fun CreateRouteAlertDialog(
    onDismissRequest: () -> Unit,
    onCreateRoute: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.create_new_route)) },
        text = {
            // Placeholder for route details input fields
            Column {
                TextField(
                    value = "wefwef",
                    onValueChange = { it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onCreateRoute) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
