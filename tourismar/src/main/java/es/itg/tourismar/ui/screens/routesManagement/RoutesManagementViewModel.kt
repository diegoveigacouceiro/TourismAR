package es.itg.tourismar.ui.screens.routesManagement

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.data.repository.anchorRepository.AnchorRepository
import es.itg.tourismar.util.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class RoutesManagementViewModel @Inject constructor(
    private val repository: AnchorRepository
) : ViewModel() {

    private val _anchorRoutes = MutableLiveData<List<AnchorRoute>?>()
    val anchorRoutes: LiveData<List<AnchorRoute>?> get() = _anchorRoutes

    private val _selectedRoute = MutableLiveData<AnchorRoute?>()
    val selectedRoute: MutableLiveData<AnchorRoute?> get() = _selectedRoute

    init {
        viewModelScope.launch {
            repository.observeAnchorRoutes().collect { resource ->
                when (resource) {
                    is Resource.Success -> _anchorRoutes.value = resource.data
                    is Resource.Error -> Log.e("RoutesManagementViewModel", "Error observing anchor routes: ${resource.message}")
                    is Resource.Loading -> Log.d("RoutesManagementViewModel", "Loading observing anchor routes...")
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
            repository.readAnchorRouteById(routeId).collect{ resource ->
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
            repository.updateAnchorRoute(anchorRoute).collect{ resource ->
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
            repository.deleteAnchorRouteById(id).collect{ resource ->
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
            repository.createAnchorRoute(anchorRoute).collect { result ->
                when (result) {
                    is Resource.Loading -> println("Creating anchor route: Loading...")
                    is Resource.Success -> _selectedRoute.value = result.data
                    is Resource.Error -> println("Creating anchor route: Error - ${result.message}")
                }
            }
        }
    }
}