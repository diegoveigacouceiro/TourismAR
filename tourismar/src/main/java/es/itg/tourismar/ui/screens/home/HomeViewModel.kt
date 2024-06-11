package es.itg.tourismar.ui.screens.home

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.data.model.marker.MarkerRoute
import es.itg.tourismar.data.repository.anchorRepository.AnchorRepository
import es.itg.tourismar.data.repository.markerRepository.MarkerRepository
import es.itg.tourismar.data.repository.storageRepository.StorageRepository
import es.itg.tourismar.util.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val anchorRepository: AnchorRepository,
    private val markerRepository: MarkerRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {
    private val _anchorRoutes = MutableLiveData<List<AnchorRoute>?>()
    val anchorRoutes: LiveData<List<AnchorRoute>?> get() = _anchorRoutes

    private val _markerRoutes = MutableLiveData<List<MarkerRoute>?>()
    val markerRoutes: LiveData<List<MarkerRoute>?> get() = _markerRoutes

    private val _searchText = MutableLiveData<String>()
    val searchText: LiveData<String> get() = _searchText

    private val _filteredAnchorRoutes = MutableLiveData<List<AnchorRoute>>()
    val filteredAnchorRoutes: LiveData<List<AnchorRoute>> get() = _filteredAnchorRoutes

    private val _filteredMarkerRoutes = MutableLiveData<List<MarkerRoute>>()
    val filteredMarkerRoutes: LiveData<List<MarkerRoute>> get() = _filteredMarkerRoutes

    private val _imageUrls = MutableLiveData<Map<String, Uri>>()
    val imageUrls: LiveData<Map<String, Uri>> get() = _imageUrls


    init {
        startObserveAnchorRoutes()
        startObserveMarkerRoutes()
        onSearchTextChanged("")
        _imageUrls.value = emptyMap()
    }

    private fun startObserveAnchorRoutes(){
        viewModelScope.launch {
            anchorRepository.observeAnchorRoutes().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        Log.d("HomeViewModel", "Anchor routes loaded: ${resource.data}")
                        _anchorRoutes.value = resource.data
                        filterRoutes(_searchText.value ?: "")
                    }
                    is Resource.Error -> Log.e("HomeViewModel", "Error observing anchor routes: ${resource.message}")
                    is Resource.Loading -> Log.d("HomeViewModel", "Loading observing anchor routes...")
                }
            }
        }
    }

    private fun startObserveMarkerRoutes(){
        viewModelScope.launch {
            markerRepository.observeMarkerRoutes().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        Log.d("HomeViewModel", "Anchor routes loaded: ${resource.data}")
                        _markerRoutes.value = resource.data
                        filterRoutes(_searchText.value ?: "")
                    }
                    is Resource.Error -> Log.e("HomeViewModel", "Error observing marker routes: ${resource.message}")
                    is Resource.Loading -> Log.d("HomeViewModel", "Loading observing marker routes...")
                }
            }
        }
    }

    private fun filterRoutes(searchText: String) {
        val anchorRoutes = _anchorRoutes.value ?: emptyList()
        val markerRoutes = _markerRoutes.value ?: emptyList()

        _filteredAnchorRoutes.value = if (searchText.isBlank()) {
            anchorRoutes
        } else {
            anchorRoutes.filter {
                it.anchorRouteName.contains(searchText, ignoreCase = true) || it.description.contains(searchText, ignoreCase = true)
            }
        }

        _filteredMarkerRoutes.value = if (searchText.isBlank()) {
            markerRoutes
        } else {
            markerRoutes.filter {
                it.name.contains(searchText, ignoreCase = true) || it.description.contains(searchText, ignoreCase = true)
            }
        }
    }

    fun onSearchTextChanged(newSearchText: String) {
        _searchText.value = newSearchText
        filterRoutes(newSearchText)
    }


    fun getImage(imageName: String, routeId: String) {
        viewModelScope.launch {
            storageRepository.loadImageFromStorage(imageName).collect { result ->
                when (result) {
                    is Resource.Loading -> Log.d("RoutesManagementViewModel","Saving image: Loading...")
                    is Resource.Success -> {
                        _imageUrls.value = _imageUrls.value?.toMutableMap()?.apply {
                            result.data?.let { put(routeId, it.normalizeScheme()) }
                        }
                    }
                    is Resource.Error -> Log.e("RoutesManagementViewModel","Save image: Error - ${result.message}")
                }
            }
        }
    }
}


