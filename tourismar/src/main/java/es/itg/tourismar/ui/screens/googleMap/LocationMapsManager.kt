package es.itg.tourismar.ui.screens.googleMap
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng

class LocationMapsManager(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {
    var currentLocation by mutableStateOf<LatLng?>(null)
        private set

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(locationCallback: LocationCallback) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,1000)
            .setIntervalMillis(10000)
            .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    fun stopLocationUpdates(locationCallback: LocationCallback) {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun handleLocationResult(locationResult: LocationResult?) {
        locationResult ?: return
        for (location in locationResult.locations) {
            currentLocation = LatLng(location.latitude, location.longitude)
            return
        }
    }
}

