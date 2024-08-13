package es.itg.tourismar.ui.screens.routesManagement

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.itg.tourismar.R
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.navigation.Screens
import es.itg.tourismar.ui.screens.googleMap.MapComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import es.itg.tourismar.util.RequestMultiplePermissionsComposable


@Composable
fun RoutesManagementScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: RoutesManagementViewModel = hiltViewModel()
) {

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navController.currentBackStackEntry?.savedStateHandle?.set("selectedRoute", AnchorRoute())
                    navController.navigate(Screens.EditAnchorRoute.route)
                          },
                modifier = modifier.padding(bottom = 50.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_route))
                Text(text = stringResource(R.string.add_route))
            }
        },
        content = { innerModifier ->
            RoutesManagementContent(
                viewModel = viewModel,
                modifier = modifier.padding(innerModifier),
                navController = navController
            )
        }
    )
}

@Composable
fun RoutesManagementContent(
    viewModel: RoutesManagementViewModel,
    modifier: Modifier,
    navController: NavController
) {
    val anchorRoutesState by viewModel.anchorRoutes.observeAsState(emptyList())

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        if (anchorRoutesState != null) {
            Text(
                text = stringResource(R.string.existing_routes),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp ,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            AnchorRoutesGrid(
                anchorRoutes = anchorRoutesState!!,
                modifier = Modifier.padding(vertical = 8.dp),
                navController = navController,
                viewModel = viewModel
            ) { anchorRoute ->
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "selectedRoute", anchorRoute
                )
                navController.navigate(Screens.EditAnchorRoute.route)
            }
        }
        Spacer(modifier = Modifier.height(56.dp))
    }
}



@Composable
fun AnchorRoutesGrid(
    anchorRoutes: List<AnchorRoute>,
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: RoutesManagementViewModel,
    onAnchorRouteClicked: (AnchorRoute)-> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .height(LocalConfiguration.current.screenHeightDp.dp)
            .padding(horizontal = 16.dp)
    ) {
        items(anchorRoutes) { anchorRoute ->
            AnchorRouteCard(anchorRoute = anchorRoute, routesManagementViewModel = viewModel){
                onAnchorRouteClicked(anchorRoute)            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnchorRouteCard(
    anchorRoute: AnchorRoute,
    routesManagementViewModel: RoutesManagementViewModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        onClick = { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(120.dp)
            ) {
                CustomImage(
                    imageName = anchorRoute.imageUrl,
                    routesManagementViewModel = routesManagementViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = anchorRoute.anchorRouteName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    fontFamily = FontFamily.Serif,
                    fontSize = TextUnit.Unspecified,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = anchorRoute.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily.Serif,
                    fontSize = TextUnit.Unspecified,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )
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
}


@Composable
fun CustomImage(
    imageName: String,
    routesManagementViewModel: RoutesManagementViewModel,
    modifier: Modifier = Modifier,
    placeholder: Int = R.drawable.torre_de_hercules
) {
    val imageUrl by routesManagementViewModel.imageUrls.observeAsState(emptyMap())

    LaunchedEffect(imageName) {
        if (imageName.isNotEmpty()) {
            routesManagementViewModel.getImage(imageName, imageName)
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