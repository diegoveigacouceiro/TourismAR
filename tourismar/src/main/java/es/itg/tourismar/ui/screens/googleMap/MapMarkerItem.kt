package es.itg.tourismar.ui.screens.googleMap

import com.google.android.gms.maps.model.LatLng

data class MapMarkerItem(
    val latLng: LatLng,
    val title: String,
    val iconResourceId: Int
)
