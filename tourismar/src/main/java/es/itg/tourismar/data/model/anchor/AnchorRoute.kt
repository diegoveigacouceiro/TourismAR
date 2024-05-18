package es.itg.tourismar.data.model.anchor

import android.net.Uri
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import es.itg.tourismar.R
import es.itg.tourismar.ui.screens.googleMap.MapMarkerItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnchorRoute(
    val id: String,
    val anchorRouteName: String,
    var anchors: List<Anchor>,
    val imageUrl: String,
    val description: String
): Parcelable{

    constructor() : this("","", emptyList(), "", "")
    fun toPartialMap(): Map<String, Any?> {
        return mapOf(
            "anchorRouteName" to anchorRouteName,
            "imageUrl" to imageUrl,
            "description" to description,
            "anchors" to anchors.map { it.toMap() }
        )
    }


    fun getAnchorLocations(): List<MapMarkerItem> {
        return anchors.map { anchor ->
            MapMarkerItem(
                latLng = LatLng(anchor.location.latitude, anchor.location.longitude),
                title = anchor.name,
                iconResourceId = R.drawable.googleg_standard_color_18
            )
        }
    }

}


