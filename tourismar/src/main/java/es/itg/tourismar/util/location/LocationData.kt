package es.itg.geoar.location

import android.location.Location
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationData(val id: String, val latitude: Double, val longitude: Double) : Parcelable {
    fun toLocation(): Location {
        val location = Location("LocationData")
        location.latitude = latitude
        location.longitude = longitude
        return location
    }
}