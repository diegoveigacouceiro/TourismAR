package es.itg.tourismar.ui.screens.arscreen

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itg.geoar.location.LocationData
import es.itg.tourismar.util.location.LocationService
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.data.repository.anchorRepository.AnchorRepository
import es.itg.tourismar.data.repository.storageRepository.ModelData
import es.itg.tourismar.data.repository.storageRepository.StorageRepository
import es.itg.tourismar.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


import java.nio.Buffer
import javax.inject.Inject

@HiltViewModel
class ARSceneViewModel @Inject constructor(
    private val anchorRepository: AnchorRepository,
    private val storageRepository: StorageRepository,
    application: Application
) : AndroidViewModel(application) {
    private val _models = MutableStateFlow<Resource<List<String>>>(Resource.Loading())
    val models: StateFlow<Resource<List<String>>> get() = _models

    @SuppressLint("StaticFieldLeak")
    private val context: Context = getApplication<Application>().applicationContext

    private var targetLocations = ArrayList<LocationData>()

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
                    is Resource.Success -> onSuccess("AnchorRoute updated")
                    is Resource.Error -> onFailure(Exception(resource.message))
                    is Resource.Loading -> Log.d("Firebase","Updating anchorRoute")
                }

            }
        }
    }

    fun startLocationService() {
        val intent = Intent(context, LocationService::class.java).apply {
            putExtra("targetLocations", targetLocations)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
            Log.d("AR","startservice")

        } else {
            context.startService(intent)
            Log.d("AR","startservice")

        }
    }

    fun stopLocationService(){
        val serviceIntent = Intent(context, LocationService::class.java)
        context.stopService(serviceIntent)
        Log.d("AR","stopservice")
    }

    fun setTargetLocations(locations: ArrayList<LocationData>) {
        targetLocations = locations
    }
}