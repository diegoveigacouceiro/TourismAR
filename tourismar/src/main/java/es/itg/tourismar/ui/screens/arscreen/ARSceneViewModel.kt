package es.itg.tourismar.ui.screens.arscreen

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingFailureReason
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.data.repository.anchorRepository.AnchorRepository
import es.itg.tourismar.data.repository.storageRepository.ModelData
import es.itg.tourismar.data.repository.storageRepository.StorageRepository
import es.itg.tourismar.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


import java.nio.Buffer
import javax.inject.Inject

@HiltViewModel
class ARSceneViewModel @Inject constructor(
    private val anchorRepository: AnchorRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {
    private val _models = MutableStateFlow<Resource<List<String>>>(Resource.Loading())
    val models: StateFlow<Resource<List<String>>> get() = _models

    init {
        fetchModels()
    }



    suspend fun loadModelData(modelName: String, resourceResolver: suspend (String) -> Buffer?): ModelData? {
        return try {
            storageRepository.loadModelDataFromStorage(modelName, resourceResolver)
        } catch (e: Exception) {
            Log.e("ViewModel", "Error al cargar el modelo: ${e.message}")
            null
        }
    }

    private fun fetchModels() {
        viewModelScope.launch {
            storageRepository.getFileNames().collect { resource ->
                when (resource) {
                    is Resource.Success -> _models.value = resource
                    is Resource.Error -> {
                        _models.value = Resource.Error(resource.message!!)
                        Log.e("Firebase", "Error fetching models: ${resource.message}")
                    }
                    is Resource.Loading -> {
                        _models.value = Resource.Loading()
                        Log.d("Firebase", "Loading models from storage...")
                    }
                }
            }
        }
    }

    fun updateAnchorRoute(anchorRoute: AnchorRoute, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit){
        viewModelScope.launch {
            anchorRepository.updateAnchorRoute(anchorRoute).collect{ resource ->
                when(resource){
                    is Resource.Success -> onSuccess("AnchorRoute updated: ${resource.message}")
                    is Resource.Error -> onFailure(Exception(resource.message))
                    is Resource.Loading -> Log.d("Firebase","Updating anchorRoute")
                }

            }
        }
    }

}