package es.itg.tourismar.data.repository.storageRepository

import android.content.Context
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import es.itg.tourismar.util.Resource
import io.github.sceneview.utils.readBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.Buffer
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage,
    private val context: Context
) : StorageRepository {
    private val MODELS_PATH = "models/"
    /**
     * Descarga el archivo GLB desde Firebase Storage y devuelve el objeto necesario
     * para crear el modelo con createModel.
     *
     * @param modelName Nombre del modelo
     * @param resourceResolver Resuelve los recursos necesarios para el modelo.
     * @return Objeto que contiene el buffer y el resolvedor de recursos.
     */
    override suspend fun loadModelDataFromStorage(modelName: String, resourceResolver: suspend (String) -> Buffer?): ModelData? {
        return try {
            // Verificar si el archivo ya está descargado localmente
            val localFile = getLocalFile(modelName)
            if (localFile.exists()) {
                // Si el archivo local existe, utilizarlo directamente
                ModelData(localFile.readBuffer(), resourceResolver)
            } else {
                // Si no existe localmente, descargar el archivo
                val glbFile = downloadFileFromStorage(modelName)
                ModelData(glbFile.readBuffer(), resourceResolver)
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Error al construir el modelData")
            e.printStackTrace()
            null
        }
    }


    /**
     * Si existe el fichero con el nombre especificado
     * lo obtiene, si no lo crea
     *
     * @param modelName La ruta del archivo en la caché de la app
     * @return Objeto File que representa la ubicación del archivo local
     * en el directorio de caché de la aplicación.
     */
    private fun getLocalFile(modelName: String): File {
        return File(context.filesDir, modelName)
    }

    /**
     * Descarga un archivo desde Firebase Storage y lo guarda localmente.
     *
     * @param modelName La referencia al archivo en Firebase Storage.
     * @return El archivo local descargado.
     */
    private suspend fun downloadFileFromStorage(modelName: String): File = withContext(Dispatchers.IO) {
        val localFile = getLocalFile(modelName)
        try {
            val fileReference = storage.reference.child(MODELS_PATH+modelName)
            fileReference.getFile(localFile).await()
            localFile
        } catch (e: Exception) {
            Log.e("Firebase", "Error al descargar el archivo: ${e.message}")
            localFile.delete()
            throw e
        }
    }

    /**
     * Obtiene de la storageReference los archivos que hay en ese path
     *
     * @return Una lista de strings que contiene los nombres de los ficheros
     */
    override fun getFileNames(): Flow<Resource<List<String>>> {
        return flow {
            emit(Resource.Loading())

            val listResult = storage.reference.listAll().await()
            val fileNames = listResult.items.map { it.name }
            emit(Resource.Success(fileNames))
        }.catch { e ->
            Log.e("Firebase", "Error al obtener la lista de archivos: ${e.message}")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }.flowOn(Dispatchers.IO)
    }
}
