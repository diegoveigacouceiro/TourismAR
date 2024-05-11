package es.itg.tourismar.ui.screens.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.AnimBuilder
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import es.itg.tourismar.R
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.navigation.Screens


@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier=Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        HomeScreenContent(viewModel,modifier, navController)
    }
}



@Composable
fun HomeScreenContent(
    viewModel: HomeViewModel,
    modifier: Modifier,
    navController: NavController
) {
    var anchorRoutesState by remember { mutableStateOf<List<AnchorRoute>?>(null) }
    var selectedAnchorRoute by remember { mutableStateOf<AnchorRoute?>(null) }

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

            SearchBar(Modifier.padding(horizontal = 20.dp))

            HomeSection(title = R.string.app_name) {
                anchorRoutesState?.let { routes ->
                    AnchorRoutesGrid(routes, modifier, navController) {
                        selectedAnchorRoute = it
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HomeSection(title = R.string.app_name) {
                anchorRoutesState?.let { routes ->
                    AnchorRoutesGrid(routes, modifier, navController) { anchorRoute ->
                        selectedAnchorRoute = anchorRoute
                    }
                }
            }
        }

        selectedAnchorRoute?.let { anchorRoute ->
            Dialog(
                onDismissRequest = { selectedAnchorRoute = null },
                properties = DialogProperties(dismissOnClickOutside = true)
            ) {
                DetailedAnchorRouteCard(
                    anchorRoute = anchorRoute,
                    modifier = Modifier
                        .align(Alignment.Center),
                    onBackClicked = {
                        navController.currentBackStackEntry?.savedStateHandle?.set("anchorRoute",anchorRoute)
                        navController.navigate(Screens.ARScene.route)

                        selectedAnchorRoute = null }
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
                .paddingFromBaseline(top = 40.dp, bottom = 16.dp)
        )
        content()
    }
}

@Composable
fun AnchorRoutesGrid(
    anchorRoutes: List<AnchorRoute>,
    modifier: Modifier = Modifier,
    navController: NavController,
    onAnchorRouteClicked: (AnchorRoute)-> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(168.dp)
            .fillMaxWidth()
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
                GoogleMap(
                    modifier=Modifier.height(400.dp),
                    properties = MapProperties(true,true,true,
                        true,null,null
                    )
                )
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
