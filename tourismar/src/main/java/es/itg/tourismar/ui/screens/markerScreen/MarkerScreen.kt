package es.itg.tourismar.ui.screens.markerScreen

import android.Manifest
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ShapeDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.itg.tourismar.data.model.marker.MarkerRoute
import es.itg.tourismar.ui.screens.googleMap.MapComposable
import es.itg.tourismar.ui.screens.home.CustomImage
import es.itg.tourismar.ui.screens.home.HomeViewModel
import es.itg.tourismar.util.RequestMultiplePermissionsComposable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkerRouteCard(
    markerRoute: MarkerRoute,
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
                imageName = markerRoute.imageUrl,
                homeViewModel = homeViewModel,
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = markerRoute.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = markerRoute.description,
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
                    text = "${markerRoute.markers.size} anchors",
                    style = MaterialTheme.typography.bodyMedium
                )
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
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = markerRoute.description,
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
                    elevation =  CardDefaults.elevatedCardElevation(),
                    colors = CardDefaults.elevatedCardColors(),
                ) {
                    MapComposable(markerRoute = markerRoute)
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

