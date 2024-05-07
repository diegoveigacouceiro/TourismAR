package es.itg.tourismar.data.model.anchor

import dev.romainguy.kotlin.math.Float3
import java.io.Serializable

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
data class Anchor(
    val id: String = "",
    val model: String = "",
    val name: String = "",
    val order: Int = 0,
    val serializedTime: String = "",
    val location: CustomLatLng = CustomLatLng(0.0, 0.0),
    val pose: Pose,
    val apiLink: String = ""
){
    constructor() : this("", "", "", 0, "", CustomLatLng(0.0, 0.0), Pose(), "")

}


data class CustomLatLng(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
): Serializable


data class SerializableFloat3(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
): Serializable

data class Pose(
    var rotation: SerializableFloat3 = SerializableFloat3(),
    var translation: SerializableFloat3 = SerializableFloat3()
): Serializable

