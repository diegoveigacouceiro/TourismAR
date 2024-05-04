package es.itg.tourismar.ui.screens.home

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

    private val _anchorRoutes = MutableLiveData<Resource<List<AnchorRoute>>>()
    val anchorRoutes: LiveData<Resource<List<AnchorRoute>>> = _anchorRoutes


    init {
        testAnchorRouteRepository()
        fetchAllAnchorRoutes()
    }

    private fun fetchAllAnchorRoutes() = viewModelScope.launch {
            _anchorRoutes.value = Resource.Loading()
            repository.readAnchorRoutes().collect { result ->
                _anchorRoutes.value = result
            }
        }


    private fun testAnchorRouteRepository() {
        // Crear una nueva ruta de ancla
        val anchor1 = Anchor(
            id = "1",
            location = CustomLatLng(43.3316415, -8.3923496),
            model = "model1.glb",
            name = "anchor1",
            order = 1,
            pose = Pose(SerializableFloat3(0f, 0f, 0f), SerializableFloat3(0f, 0f, 0f)),
            serializedTime = "05/03/2024 17:22:11"
        )
        val anchor2 = Anchor(
            id = "2",
            location = CustomLatLng(43.3316415, -8.3923496),
            model = "model.glb",
            name = "anchor2",
            order = 1,
            pose = Pose(SerializableFloat3(0f, 0f, 0f), SerializableFloat3(0f, 0f, 0f)),
            serializedTime = "05/03/2024 17:22:11"
        )
        val anchorRoute = AnchorRoute(
            anchorRouteName = "Route 2",
            anchors = listOf(anchor1, anchor2),
            imageUrl = "route2.jpg",
            description = "Route 2 Description"
        )

        viewModelScope.launch {
            // Crear una nueva ruta de ancla
            repository.createAnchorRoute(anchorRoute).collect { result ->
                when (result) {
                    is Resource.Loading -> println("Creating anchor route: Loading...")
                    is Resource.Success -> println("Creating anchor route: Success! ${result.data}")
                    is Resource.Error -> println("Creating anchor route: Error - ${result.message}")
                }
            }

            // Leer la ruta de ancla recién creada por ID
            repository.readAnchorRouteById("1").collect { result ->
                when (result) {
                    is Resource.Loading -> println("Reading anchor route: Loading...")
                    is Resource.Success -> println("Reading anchor route: Success! ${result.data}")
                    is Resource.Error -> println("Reading anchor route: Error - ${result.message}")
                }
            }


            // Actualizar la ruta de ancla recién creada
            val updatedAnchorRoute = anchorRoute.copy(description = "Updated Route Description")
            repository.updateAnchorRoute(updatedAnchorRoute).collect { result ->
                when (result) {
                    is Resource.Loading -> println("Updating anchor route: Loading...")
                    is Resource.Success -> println("Updating anchor route: Success!")
                    is Resource.Error -> println("Updating anchor route: Error - ${result.message}")
                }
            }

            // Eliminar la ruta de ancla recién creada
            repository.deleteAnchorRouteById("1").collect { result ->
                when (result) {
                    is Resource.Loading -> println("Deleting anchor route: Loading...")
                    is Resource.Success -> println("Deleting anchor route: Success!")
                    is Resource.Error -> println("Deleting anchor route: Error - ${result.message}")
                }
            }
        }
    }
}
