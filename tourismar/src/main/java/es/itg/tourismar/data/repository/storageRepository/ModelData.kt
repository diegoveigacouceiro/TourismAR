package es.itg.tourismar.data.repository.storageRepository

import java.nio.Buffer


/**
 * Clase para contener el buffer del modelo y el resolvedor de recursos.
 */
data class ModelData(val buffer: Buffer, val resourceResolver: suspend (String) -> Buffer?)