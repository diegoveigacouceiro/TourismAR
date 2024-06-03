package es.itg.tourismar.data.repository.markerRepository

import com.google.android.gms.tasks.Task
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.data.model.marker.MarkerRoute
import es.itg.tourismar.util.Resource
import kotlinx.coroutines.flow.Flow

interface MarkerRepository {

    fun readMarkerRoutes(): Flow<Resource<List<MarkerRoute>>>
    fun observeMarkerRoutes(): Flow<Resource<List<MarkerRoute>>>
    fun readMarkerRouteById(id: String): Flow<Resource<MarkerRoute?>>
}