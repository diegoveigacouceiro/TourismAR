package es.itg.tourismar.ui.screens.arscreen.controllers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.ar.core.Session
import es.itg.geoar.location.LocationData
import es.itg.tourismar.data.model.anchor.Anchor
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.data.model.anchor.CustomLatLng
import es.itg.tourismar.data.model.anchor.HostingState
import es.itg.tourismar.ui.screens.arscreen.ARSceneViewModel
import io.github.sceneview.ar.node.AnchorNode
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException


suspend fun getLocation(context: Context): CustomLatLng {
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    val location: Location? = try {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.await()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }

    return if (location != null) {
        CustomLatLng(location.latitude, location.longitude)
    } else {
        CustomLatLng(0.0, 0.0)
    }
}

@Composable
fun LocationServiceHandler(
    arSceneViewModel: ARSceneViewModel,
    anchorRoute: AnchorRoute?,
) {

    LaunchedEffect(anchorRoute) {
        if (anchorRoute != null) {
            if (anchorRoute.anchors.isNotEmpty()) {
                arSceneViewModel.setTargetLocations(createLocationDataListAnchor(anchorRoute.anchors))
                arSceneViewModel.startLocationService()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            arSceneViewModel.stopLocationService()
            Log.d("ARSceneScreen","StopLocation")
        }
    }
}

private fun createLocationDataListAnchor(anchorList: List<Anchor>): ArrayList<LocationData> {
    val locationDataList = ArrayList<LocationData>()
    anchorList.forEach {
        locationDataList.add(LocationData(it.id,it.location.latitude,it.location.longitude))
    }

    return locationDataList
}

suspend fun testAnchorQuality(arSceneController: ARSceneController, session: Session, anchorNode: AnchorNode) {
    val qualityChecker = QualityChecker(
        anyQualityThresholdReached = {
            with(arSceneController) {
                hostingState = HostingState.READY_TO_HOST
                scanningMessage = "Calidade suficiente para crear unha Cloud Anchor"
            }
        },
        bothQualityThresholdsReached = {
            with(arSceneController) {
                hostingState = HostingState.READY_TO_HOST
                scanningMessage = "Calidade óptima para crear unha Cloud Anchor"
            }
        }
    )
    try {
        qualityChecker.checkQualityAndProceed(session, arSceneController, anchorNode)
    } catch (e: CancellationException) {
        // Handle the cancellation here if necessary
        // For example, logging or updating the UI
    } catch (e: Exception) {
        // Handle other exceptions
        // For example, logging or showing an error message
    }
}
