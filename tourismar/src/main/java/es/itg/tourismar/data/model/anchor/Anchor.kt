package es.itg.tourismar.data.model.anchor

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * Tipo de dato usado para guardar la información relacionada con una
 * CloudAnchor
 * @param id Identificador único de una Cloud Anchor
 * @param model Modelo que será visualizado al resolver el ancla
 * @param name Nombre descriptivo asociado al Cloud Anchor
 * @param order Número del Anchor en la BD a la hora de ser creado
 * @param serializedTime Hora y día de creación del Cloud Anchor
 * @param location Coordenadas del lugar en la que se encuentra el Cloud Anchor
 * @param pose Pose utilizada para orientar y posicionar el modelo renderizado
 * una vez resuelto el Cloud Anchor
 * @param apiLink Link o endpoint para mostrar contenido alojado en un ApiRest
 *
 */

@Parcelize
data class Anchor(
    val id: String,
    val model: String,
    val name: String,
    val order: Int,
    val serializedTime: String,
    val location: CustomLatLng,
    val pose: Pose,
    val apiLink: String
) : Parcelable{
    constructor() : this("", "", "", 0, "", CustomLatLng(0.0, 0.0), Pose(), "")
}

@Parcelize
data class CustomLatLng(
    val latitude: Double,
    val longitude: Double
) : Parcelable{
    constructor() : this(0.0, 0.0)
}

@Parcelize
data class SerializableFloat3(
    val x: Float,
    val y: Float,
    val z: Float
) : Parcelable{
    constructor() : this(0f, 0f, 0f)
}

@Parcelize
data class Pose(
    var rotation: SerializableFloat3,
    var translation: SerializableFloat3
) : Parcelable{
    constructor() : this(SerializableFloat3(), SerializableFloat3())
}


