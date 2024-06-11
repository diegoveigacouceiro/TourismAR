package es.itg.tourismar.ui.screens.markerScreen

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ShapeDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.itg.tourismar.R
import es.itg.tourismar.data.model.marker.MarkerRoute
import es.itg.tourismar.ui.screens.googleMap.MapComposable
import es.itg.tourismar.ui.screens.home.CustomImage
import es.itg.tourismar.ui.screens.home.HomeViewModel
import es.itg.tourismar.ui.screens.home.RequestMultiplePermissionsComposable



@Composable
fun MarkerRouteCard(
    markerRoute: MarkerRoute,
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
                    imageName = markerRoute.imageUrl,
                    homeViewModel = homeViewModel,
                    modifier = Modifier.size(80.dp),
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
                        text = markerRoute.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = markerRoute.description,
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
                text = markerRoute.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Description: ${markerRoute.description}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
//            Text(
//                text = "Location: ${markerRoute.markers[0].location}",
//                style = MaterialTheme.typography.bodySmall,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
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

