package es.itg.tourismar.ui.screens.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itg.tourismar.data.model.anchor.Anchor
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.data.model.anchor.CustomLatLng
import es.itg.tourismar.data.model.anchor.Pose
import es.itg.tourismar.data.model.anchor.SerializableFloat3
import es.itg.tourismar.data.repository.anchorRepository.AnchorRepository
import es.itg.tourismar.util.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
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


    init {
//        testAnchorRouteRepository()
//        fetchAnchorRoutes()
    }


//    fun fetchAnchorRoutes() {
//        viewModelScope.launch {
//            _anchorRoutes.value = Resource.Loading()
//            try {
//                val result = repository.readAnchorRoutes()
//                _anchorRoutes.value = Resource.Success(result)
//            } catch (e: Exception) {
//                _anchorRoutes.value = Resource.Error(e.message ?: "An error occurred")
//            }
//        }




    private fun testAnchorRouteRepository() {
        // Crear una nueva ruta de ancla
        val anchor1 = Anchor(
           "1",
            "model1.glb",
            "anchor1",
            1,
            "05/03/2024 17:22:11",
            CustomLatLng(43.3316415, -8.3923496),
            Pose(SerializableFloat3(0f, 0f, 0f), SerializableFloat3(0f, 0f, 0f)),
             "05/03/2024 17:22:11"
        )

        val anchor2 = Anchor(
            "2",
            "model2.glb",
            "anchor2",
            2,
            "05/03/2024 17:22:11",
            CustomLatLng(43.3316415, -8.3923496),
            Pose(SerializableFloat3(0f, 0f, 0f), SerializableFloat3(0f, 0f, 0f)),
            "05/03/2024 17:22:11"
        )

        val anchorRoute = AnchorRoute("Route 2","Route 2",listOf(anchor1, anchor2),"images/torre_de_hercules.jpeg","Route 2 Description")


        viewModelScope.launch {
            // Crear una nueva ruta de ancla
            repository.createAnchorRoute(anchorRoute).collect { result ->
                when (result) {
                    is Resource.Loading -> println("Creating anchor route: Loading...")
                    is Resource.Success -> println("Creating anchor route: Success! ${result.data}")
                    is Resource.Error -> println("Creating anchor route: Error - ${result.message}")
                }
            }
//
//            // Leer la ruta de ancla recién creada por ID
//            repository.readAnchorRouteById("1").collect { result ->
//                when (result) {
//                    is Resource.Loading -> println("Reading anchor route: Loading...")
//                    is Resource.Success -> println("Reading anchor route: Success! ${result.data}")
//                    is Resource.Error -> println("Reading anchor route: Error - ${result.message}")
//                }
//            }
//
//
//            // Actualizar la ruta de ancla recién creada
//            val updatedAnchorRoute = anchorRoute.copy(description = "Updated Route Description")
//            repository.updateAnchorRoute(updatedAnchorRoute).collect { result ->
//                when (result) {
//                    is Resource.Loading -> println("Updating anchor route: Loading...")
//                    is Resource.Success -> println("Updating anchor route: Success!")
//                    is Resource.Error -> println("Updating anchor route: Error - ${result.message}")
//                }
//            }
//
//            // Eliminar la ruta de ancla recién creada
//            repository.deleteAnchorRouteById("1").collect { result ->
//                when (result) {
//                    is Resource.Loading -> println("Deleting anchor route: Loading...")
//                    is Resource.Success -> println("Deleting anchor route: Success!")
//                    is Resource.Error -> println("Deleting anchor route: Error - ${result.message}")
//                }
//            }
        }
    }

}
