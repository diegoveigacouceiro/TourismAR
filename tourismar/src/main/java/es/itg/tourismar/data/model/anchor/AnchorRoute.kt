package es.itg.tourismar.data.model.anchor

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnchorRoute(
    val id: String = "",
    val anchorRouteName: String="",
    val anchors: List<Anchor> = listOf(),
    val imageUrl: String="",
    val description: String = ""
):Parcelable{
    fun toUriString(): String {
        return Uri.Builder()
            .scheme("your_scheme")
            .authority("your_authority")
            .appendPath(anchorRouteName)
            .build()
            .toString()
    }

    constructor() : this("","", emptyList(), "", "")
}
