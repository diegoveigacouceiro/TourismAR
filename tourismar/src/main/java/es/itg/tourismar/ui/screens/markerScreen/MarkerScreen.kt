package es.itg.tourismar.ui.screens.markerScreen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import es.itg.tourismar.R
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.data.model.marker.MarkerRoute
import es.itg.tourismar.navigation.Screens
import es.itg.tourismar.ui.screens.googleMap.MapComposable
import es.itg.tourismar.ui.screens.home.HomeViewModel


@Composable
fun MarkerScreen(
    navController: NavController,
    modifier: Modifier=Modifier,
    viewModel: MarkerViewModel = hiltViewModel()
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        MarkerScreenContent(viewModel,modifier, navController)
    }
}



@Composable
fun MarkerScreenContent(
    viewModel: MarkerViewModel,
    modifier: Modifier,
    navController: NavController
) {
    var markerRoutesState by remember { mutableStateOf<List<MarkerRoute>?>(null) }
    var selectedMarkerRoute by remember { mutableStateOf<MarkerRoute?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.markerRoutes.observeForever { markerRoutes ->
            markerRoutesState = markerRoutes
        }
    }

    Box(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize(1f)
    ) {
        Column {
            Spacer(modifier = Modifier.height(20.dp))

            SearchBar(Modifier.padding(horizontal = 20.dp))

            MarkerSection(title = R.string.app_name) {
                markerRoutesState?.let { routes ->
                    MarkerRoutesGrid(routes, modifier, navController) {
                        selectedMarkerRoute = it
                    }
                }
            }
        }

        selectedMarkerRoute?.let { markerRoute ->
            Dialog(
                onDismissRequest = { selectedMarkerRoute = null },
                properties = DialogProperties(dismissOnClickOutside = true)
            ) {
                DetailedMarkerRouteCard(
                    markerRoute = markerRoute,
                    modifier = Modifier
                        .align(Alignment.Center),
                    onBackClicked = {
                        navController.currentBackStackEntry?.savedStateHandle?.set("markerRoute",markerRoute)
                        navController.navigate(Screens.ARScene.route)

                        selectedMarkerRoute = null }
                )
            }
        }
    }
}




@Composable
fun MarkerSection(
    @StringRes title: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .paddingFromBaseline(top = 40.dp, bottom = 16.dp)
        )
        content()
    }
}

@Composable
fun MarkerRoutesGrid(
    markerRoutes: List<MarkerRoute>,
    modifier: Modifier = Modifier,
    navController: NavController,
    onAnchorRouteClicked: (MarkerRoute) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .height(LocalConfiguration.current.screenHeightDp.dp)
    ) {
        items(markerRoutes) { markerRoute ->
            MarkerRouteCard(markerRoute = markerRoute) {
                onAnchorRouteClicked(markerRoute)
            }
        }
    }
}


@OptIn(ExperimentalCoilApi::class)
@Composable
fun MarkerRouteCard(
    markerRoute: MarkerRoute,
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
                        text = markerRoute.name!!,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = markerRoute.description!!,
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
fun DetailedMarkerRouteCard(
    markerRoute: MarkerRoute,
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
                text = markerRoute.name!!,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Description: ${markerRoute.description}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Location: ${markerRoute.markers[0].location}",
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
                    MapComposable(markerRoute = markerRoute)
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
fun SearchBar(
    modifier: Modifier = Modifier
) {
    TextField(
        value = "",
        onValueChange = {},
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        placeholder = {
            Text(stringResource(R.string.app_name))
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    )
}

@Composable
fun RequestMultiplePermissionsComposable(
    permissions: Array<String>,
    onPermissionsGranted: @Composable () -> Unit
) {
    var permissionsGranted by rememberSaveable { mutableStateOf(false) }
    val launcherMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsGranted = it.values.reduce { acc, next -> acc && next } }

    LaunchedEffect(Unit) {
        launcherMultiplePermissions.launch(permissions)

    }
    if (permissionsGranted){
        onPermissionsGranted()
    }
}


