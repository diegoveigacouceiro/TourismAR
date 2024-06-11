package es.itg.tourismar.ui.screens.routesManagement

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.data.repository.anchorRepository.AnchorRepository
import es.itg.tourismar.data.repository.storageRepository.StorageRepository
import es.itg.tourismar.util.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class RoutesManagementViewModel @Inject constructor(
    private val anchorRepository: AnchorRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _anchorRoutes = MutableLiveData<List<AnchorRoute>?>()
    val anchorRoutes: LiveData<List<AnchorRoute>?> get() = _anchorRoutes

    private val _selectedRoute = MutableLiveData<AnchorRoute?>()
    val selectedRoute: MutableLiveData<AnchorRoute?> get() = _selectedRoute

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: MutableLiveData<Uri?> get() = _selectedImageUri

    init {
        startObserveAnchorRoutes()
    }

    private fun startObserveAnchorRoutes(){
        viewModelScope.launch {
            anchorRepository.observeAnchorRoutes().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        Log.d("HomeViewModel", "Anchor routes loaded: ${resource.data}")
                        _anchorRoutes.value = resource.data
                    }
                    is Resource.Error -> Log.e("HomeViewModel", "Error observing anchor routes: ${resource.message}")
                    is Resource.Loading -> Log.d("HomeViewModel", "Loading observing anchor routes...")
                }
            }
        }
    }

    fun setSelectedRoute(anchorRoute: AnchorRoute) {
        _selectedRoute.value = anchorRoute

    }

    fun getAnchorRoute(routeId: String): AnchorRoute? {
        var anchorRoute: AnchorRoute? = null
        viewModelScope.launch {
            anchorRepository.readAnchorRouteById(routeId).collect{ resource ->
                when (resource) {
                    is Resource.Success -> anchorRoute = resource.data
                    is Resource.Error -> Log.e("RoutesManagementViewModel", "Error observing anchor routes: ${resource.message}")
                    is Resource.Loading -> Log.d("RoutesManagementViewModel", "Loading observing anchor routes...")
                }
            }
        }
        return anchorRoute
    }



    fun updateAnchorRoute(anchorRoute: AnchorRoute) {
        viewModelScope.launch {
            anchorRepository.updateAnchorRoute(anchorRoute).collect{ resource ->
                when (resource) {
                    is Resource.Success -> resource.data
                    is Resource.Error -> Log.e("RoutesManagementViewModel", "Error observing anchor routes: ${resource.message}")
                    is Resource.Loading -> Log.d("RoutesManagementViewModel", "Loading observing anchor routes...")
                }
            }
        }

    }

    fun deleteAnchorRoute(id: String) {
        viewModelScope.launch {
            anchorRepository.deleteAnchorRouteById(id).collect{ resource ->
                when (resource) {
                    is Resource.Success -> resource.data
                    is Resource.Error -> Log.e("RoutesManagementViewModel", "Error observing anchor routes: ${resource.message}")
                    is Resource.Loading -> Log.d("RoutesManagementViewModel", "Loading observing anchor routes...")
                }
            }
        }
    }

    fun createAnchorRoute(anchorRoute: AnchorRoute) {
        viewModelScope.launch {
            anchorRepository.createAnchorRoute(anchorRoute).collect { result ->
                when (result) {
                    is Resource.Loading -> Log.d("RoutesManagementViewModel", "Creating anchor route: Loading...")
                    is Resource.Success -> _selectedRoute.value = result.data
                    is Resource.Error -> Log.e("RoutesManagementViewModel", "Creating anchor route: Error - ${result.message}")
                }
            }
        }
    }

    fun saveImage(fileUri: Uri, imageName: String) {
        viewModelScope.launch {
            storageRepository.uploadImageToStorage(fileUri, imageName).collect { result ->
                when (result) {
                    is Resource.Loading -> Log.d("RoutesManagementViewModel","Saving image: Loading...")
                    is Resource.Success -> Log.e("RoutesManagementViewModel","Image saved")
                    is Resource.Error -> Log.e("RoutesManagementViewModel","Save image: Error - ${result.message}")
                }
            }
        }
    }

    fun getImage(imageName: String){
        viewModelScope.launch {
            storageRepository.loadImageFromStorage(imageName).collect { result ->
                when (result) {
                    is Resource.Loading -> Log.d("RoutesManagementViewModel","Saving image: Loading...")
                    is Resource.Success -> _selectedImageUri.value = result.data
                    is Resource.Error -> Log.e("RoutesManagementViewModel","Save image: Error - ${result.message}")
                }
            }
        }
    }
}