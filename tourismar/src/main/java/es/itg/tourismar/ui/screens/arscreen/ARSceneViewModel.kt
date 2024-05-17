package es.itg.tourismar.ui.screens.arscreen

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingFailureReason
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itg.tourismar.data.repository.anchorRepository.AnchorRepository
import es.itg.tourismar.data.repository.storageRepository.ModelData
import es.itg.tourismar.data.repository.storageRepository.StorageRepository



import java.nio.Buffer
import javax.inject.Inject

@HiltViewModel
class ARSceneViewModel @Inject constructor(
    private val anchorRepository: AnchorRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {



    suspend fun loadModelData(modelName: String, resourceResolver: suspend (String) -> Buffer?): ModelData? {
        return try {
            storageRepository.loadModelDataFromStorage(modelName, resourceResolver)
        } catch (e: Exception) {
            Log.e("ViewModel", "Error al cargar el modelo: ${e.message}")
            null
        }
    }

}