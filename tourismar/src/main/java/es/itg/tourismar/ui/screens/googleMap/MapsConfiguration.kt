package es.itg.tourismar.ui.screens.googleMap

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings

class MapsConfiguration {
    val properties: MutableState<MapProperties> = mutableStateOf(
        MapProperties(
            isBuildingEnabled = true,
            isIndoorEnabled = true,
            isMyLocationEnabled = true,
            isTrafficEnabled = true,
            mapType = MapType.NORMAL
        )
    )
    val uiSettings: MutableState<MapUiSettings> = mutableStateOf(
        MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = true,
            rotationGesturesEnabled = true,
            compassEnabled = true,
            indoorLevelPickerEnabled = true,
            mapToolbarEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = true,
            zoomGesturesEnabled = true
        )
    )


}
