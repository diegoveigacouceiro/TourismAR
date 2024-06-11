package es.itg.tourismar.ui.screens.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
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
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import es.itg.tourismar.R
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.data.model.marker.MarkerRoute
import es.itg.tourismar.data.model.users.UserLevel
import es.itg.tourismar.navigation.Screens
import es.itg.tourismar.ui.screens.googleMap.MapComposable
import es.itg.tourismar.ui.screens.markerScreen.DetailedMarkerRouteCard
import es.itg.tourismar.ui.screens.markerScreen.MarkerRouteCard


@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier=Modifier,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        HomeScreenContent(homeViewModel, modifier, navController)
    }
}


@Composable
fun HomeScreenContent(
    homeViewModel: HomeViewModel,
    modifier: Modifier,
    navController: NavController
) {
    val searchText by homeViewModel.searchText.observeAsState("")
    val filteredAnchorRoutes by homeViewModel.filteredAnchorRoutes.observeAsState(emptyList())
    val filteredMarkerRoutes by homeViewModel.filteredMarkerRoutes.observeAsState(emptyList())

    var selectedAnchorRoute by remember { mutableStateOf<AnchorRoute?>(null) }
    var selectedMarkerRoute by remember { mutableStateOf<MarkerRoute?>(null) }


    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column {
            Spacer(modifier = Modifier.height(20.dp))

            HomeSection(title = R.string.app_name) {

                SearchBar(
                    searchText = searchText,
                    onSearchTextChange = homeViewModel::onSearchTextChanged,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                MixedRoutesGrid(
                    anchorRoutes = filteredAnchorRoutes,
                    markerRoutes = filteredMarkerRoutes,
                    homeViewModel= homeViewModel,
                    onAnchorRouteClicked = { selectedAnchorRoute = it },
                    onMarkerRouteClicked = { selectedMarkerRoute = it },
                    modifier = modifier
                )
            }
        }

        selectedAnchorRoute?.let { anchorRoute ->
            Dialog(
                onDismissRequest = { selectedAnchorRoute = null },
                properties = DialogProperties(dismissOnClickOutside = true)
            ) {
                DetailedAnchorRouteCard(
                    anchorRoute = anchorRoute,
                    modifier = Modifier.align(Alignment.Center),
                    onBackClicked = {
                        navController.currentBackStackEntry?.savedStateHandle?.set("anchorRoute", anchorRoute)
                        navController.currentBackStackEntry?.savedStateHandle?.set("userLevel", UserLevel.NORMAL)
                        navController.navigate(Screens.ARScene.route)
                        selectedAnchorRoute = null
                    }
                )
            }
        }

        selectedMarkerRoute?.let { markerRoute ->
            Dialog(
                onDismissRequest = { selectedMarkerRoute = null },
                properties = DialogProperties(dismissOnClickOutside = true)
            ) {
                DetailedMarkerRouteCard(
                    markerRoute = markerRoute,
                    modifier = Modifier.align(Alignment.Center),
                    onBackClicked = {
                        navController.currentBackStackEntry?.savedStateHandle?.set("markerRoute", markerRoute)
                        navController.currentBackStackEntry?.savedStateHandle?.set("userLevel", UserLevel.NORMAL)
                        navController.navigate(Screens.ARScene.route)
                        selectedMarkerRoute = null
                    }
                )
            }
        }
    }
}


@Composable
fun HomeSection(
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
                .paddingFromBaseline(top = 16.dp, bottom = 16.dp)
        )
        content()
    }
}

@Composable
fun MixedRoutesGrid(
    anchorRoutes: List<AnchorRoute>,
    markerRoutes: List<MarkerRoute>,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onAnchorRouteClicked: (AnchorRoute) -> Unit,
    onMarkerRouteClicked: (MarkerRoute) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .height(LocalConfiguration.current.screenHeightDp.dp)
    ) {
        items(anchorRoutes) { anchorRoute ->
            AnchorRouteCard(anchorRoute = anchorRoute, homeViewModel = homeViewModel) {
                onAnchorRouteClicked(anchorRoute)
            }
        }
        items(markerRoutes) { markerRoute ->
            MarkerRouteCard(markerRoute = markerRoute, homeViewModel = homeViewModel) {
                onMarkerRouteClicked(markerRoute)
            }
        }
    }
}




@Composable
fun AnchorRouteCard(
    anchorRoute: AnchorRoute,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CustomImage(
                    imageName = anchorRoute.imageUrl,
                    homeViewModel = homeViewModel,
                    modifier = modifier.size(80.dp),
                    placeholder = R.mipmap.ic_torre_background
                )
                Column(
                    modifier = modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .heightIn(max = 80.dp)
                        .clip(MaterialTheme.shapes.medium),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = anchorRoute.anchorRouteName,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = anchorRoute.description,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = modifier.width(200.dp),
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
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = anchorRoute.anchorRouteName,
                style = MaterialTheme.typography.titleMedium,
                modifier = modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Description: ${anchorRoute.description}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = modifier.padding(bottom = 8.dp)
            )
//            Text(
//                text = "Location: ${anchorRoute.anchors[0].location}",
//                style = MaterialTheme.typography.bodySmall,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
            RequestMultiplePermissionsComposable(permissions = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )) {
                ElevatedCard(
                modifier = modifier
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
                modifier = modifier.align(Alignment.End)
            ) {
                Text(text = "AR")
            }
        }
    }
}



@Composable
fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = searchText,
        onValueChange = onSearchTextChange,
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
            Text(stringResource(R.string.search_placeholder))
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

@Composable
fun CustomImage(
    imageName: String,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    placeholder: Int = R.drawable.ic_torre_foreground
) {
    val imageUrl by homeViewModel.imageUrls.observeAsState(emptyMap())

    LaunchedEffect(imageName) {
        if (imageName.isNotEmpty()) {
            homeViewModel.getImage(imageName, imageName)
        }
    }

    val painter = rememberAsyncImagePainter(imageUrl[imageName] ?: placeholder)

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

