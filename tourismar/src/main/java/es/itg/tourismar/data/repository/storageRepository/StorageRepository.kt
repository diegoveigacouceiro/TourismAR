package es.itg.tourismar.data.repository.storageRepository

import es.itg.tourismar.util.Resource
import kotlinx.coroutines.flow.Flow
import java.nio.Buffer

interface StorageRepository {
    suspend fun loadModelDataFromStorage(modelName: String, resourceResolver: suspend (String) -> Buffer?): ModelData?
    fun getFileNames(): Flow<Resource<List<String>>>
}