package es.itg.tourismar.ui.screens.home

import android.Manifest
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import es.itg.tourismar.R
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.data.model.marker.MarkerRoute
import es.itg.tourismar.data.model.users.UserLevel
import es.itg.tourismar.navigation.Screens
import es.itg.tourismar.ui.screens.googleMap.MapComposable
import es.itg.tourismar.ui.screens.markerScreen.DetailedMarkerRouteCard
import es.itg.tourismar.ui.screens.markerScreen.MarkerRouteCard
import es.itg.tourismar.util.RequestMultiplePermissionsComposable


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
fun MixedRoutesGrid(
    anchorRoutes: List<AnchorRoute>,
    markerRoutes: List<MarkerRoute>,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onAnchorRouteClicked: (AnchorRoute) -> Unit,
    onMarkerRouteClicked: (MarkerRoute) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnchorRouteCard(
    anchorRoute: AnchorRoute,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .width(200.dp)
            .height(300.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        onClick = { onClick() }
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomImage(
                imageName = anchorRoute.imageUrl,
                homeViewModel = homeViewModel,
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = anchorRoute.anchorRouteName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = anchorRoute.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                overflow = TextOverflow.Ellipsis,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${anchorRoute.anchors.size} anchors",
                    style = MaterialTheme.typography.bodyMedium
                )
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
        modifier = Modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = anchorRoute.anchorRouteName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = anchorRoute.description,
                style = MaterialTheme.typography.bodyLarge,
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
                    elevation =  CardDefaults.elevatedCardElevation(8.dp),
                    colors = CardDefaults.elevatedCardColors(),
                ) {
                    MapComposable(anchorRoute = anchorRoute)
                }
            }
            ElevatedButton(
                onClick = onBackClicked,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors()
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
            .padding(16.dp)
            .heightIn(min = 56.dp)
    )
}


@Composable
fun CustomImage(
    imageName: String,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    placeholder: Int = R.drawable.googleg_standard_color_18
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

