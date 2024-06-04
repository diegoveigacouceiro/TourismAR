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

    init {
        viewModelScope.launch {
            repository.observeAnchorRoutes().collect { resource ->
                when (resource) {
                    is Resource.Success -> _anchorRoutes.value = resource.data
                    is Resource.Error -> Log.e("HomeViewModel", "Error observing anchor routes: ${resource.message}")
                    is Resource.Loading -> Log.d("HomeViewModel", "Loading observing anchor routes...")
                }
            }
        }
    }

    fun getAnchorRoute(routeId: String): AnchorRoute? {
        var anchorRoute: AnchorRoute? = null
        viewModelScope.launch {
            repository.readAnchorRouteById(routeId).collect{ resource ->
                when (resource) {
                    is Resource.Success -> anchorRoute = resource.data
                    is Resource.Error -> Log.e("HomeViewModel", "Error observing anchor routes: ${resource.message}")
                    is Resource.Loading -> Log.d("HomeViewModel", "Loading observing anchor routes...")
                }
            }
        }
        return anchorRoute
    }



    fun updateAnchorRoute(copy: AnchorRoute) {

    }

    fun deleteAnchorRoute(id: String) {

    }
}