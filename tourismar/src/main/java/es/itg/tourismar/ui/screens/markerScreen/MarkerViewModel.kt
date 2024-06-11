package es.itg.tourismar.ui.screens.markerScreen
//
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import dagger.hilt.android.lifecycle.HiltViewModel
//import es.itg.tourismar.data.model.marker.MarkerRoute
//import es.itg.tourismar.data.repository.markerRepository.MarkerRepository
//import es.itg.tourismar.util.Resource
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class MarkerViewModel @Inject constructor(
//    private val repository: MarkerRepository
//) : ViewModel() {
//    private val _markerRoutes = MutableLiveData<List<MarkerRoute>?>()
//    val markerRoutes: LiveData<List<MarkerRoute>?> get() = _markerRoutes
//
//    init {
//        viewModelScope.launch {
//            repository.observeMarkerRoutes().collect { resource ->
//                when (resource) {
//                    is Resource.Success -> _markerRoutes.value = resource.data
//                    is Resource.Error -> Log.e("HomeViewModel", "Error observing anchor routes: ${resource.message}")
//                    is Resource.Loading -> Log.d("HomeViewModel", "Loading observing anchor routes...")
//                }
//            }
//        }
//    }
//}