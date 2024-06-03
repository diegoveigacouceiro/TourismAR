package es.itg.tourismar.data.model.marker

import android.os.Parcelable
import es.itg.tourismar.data.model.anchor.CustomLatLng
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
data class Marker(
    val model: String,
    val name: String,
    val altitude: Double,
    val timestamp: String,
    val location: CustomLatLng
) : Parcelable {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "model" to model,
            "name" to name,
            "altitude" to altitude,
            "timestamp" to timestamp,
            "location" to location.toMap()
        )
    }

    constructor() : this("", "", 0.0, "", CustomLatLng(0.0, 0.0))
}
