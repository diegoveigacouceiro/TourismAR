package es.itg.tourismar.data.model.marker

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import es.itg.tourismar.R
import es.itg.tourismar.ui.screens.googleMap.MapMarkerItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class MarkerRoute(
    val id: String,
    val name: String,
    var markers: List<Marker>,
    val imageUrl: String,
    val description: String
): Parcelable {

    constructor() : this("","", emptyList(), "", "")


    fun getMarkerLocations(): List<MapMarkerItem>  {
        return markers.map { marker ->
            MapMarkerItem(
                latLng = LatLng(marker.location.latitude, marker.location.longitude),
                title = marker.name,
                iconResourceId = R.drawable.googleg_standard_color_18
            )
        }
    }

}