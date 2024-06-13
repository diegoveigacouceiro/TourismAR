package es.itg.tourismar.ui.screens.googleMap

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.StreetViewPanoramaOptions
import com.google.android.gms.maps.model.AdvancedMarker
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.streetview.StreetView
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.data.model.marker.MarkerRoute
import kotlin.math.pow
import kotlin.math.sqrt


@Composable
fun MapComposable(
    modifier: Modifier = Modifier,
    anchorRoute: AnchorRoute? = null,
    markerRoute: MarkerRoute? = null
) {
    val context = LocalContext.current
    val mapsConfiguration = remember { MapsConfiguration() }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val userLocation = remember { mutableStateOf(LatLng(43.368933825763825, -8.40165875882876)) }
    val zoomLevel = remember { mutableStateOf(19f) }
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.Builder().target(userLocation.value).zoom(zoomLevel.value).build() }
    val firstLocation = remember { mutableStateOf(true) }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { lastLocation ->
                    userLocation.value = LatLng(lastLocation.latitude, lastLocation.longitude)
                    if (firstLocation.value) {
                        cameraPositionState.move(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder().target(userLocation.value).zoom(zoomLevel.value).build()
                            )
                        )
                        firstLocation.value = false
                    }
                }
            }
        }
    }

    val locationMapsManager = remember {
        LocationMapsManager(context = context, fusedLocationClient = fusedLocationClient)
    }

    DisposableEffect(Unit) {
        locationMapsManager.startLocationUpdates(locationCallback)
        onDispose {
            locationMapsManager.stopLocationUpdates(locationCallback)
        }
    }

    val markersToRender = remember { mutableStateListOf<MapMarkerItem>() }

    // Clear previous markers to avoid duplication
    markersToRender.clear()

    // Add markers based on the provided route
    anchorRoute?.let {
        it.getAnchorLocations().forEach { location ->
            markersToRender.add(location)
        }
    }

    markerRoute?.let {
        it.getMarkerLocations().forEach { location ->
            markersToRender.add(location)
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapsConfiguration.properties.value,
        uiSettings = mapsConfiguration.uiSettings.value
    ) {
        RenderMarkers(markersToRender, cameraPositionState)
    }
}


@Composable
private fun RenderMarkers(markers: List<MapMarkerItem>, cameraPositionState: CameraPositionState) {
    markers.forEach { marker ->
        Marker(
            state = MarkerState(marker.latLng),
            title = marker.title,
        )
    }
}

