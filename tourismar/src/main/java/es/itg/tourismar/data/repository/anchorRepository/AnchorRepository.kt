package es.itg.tourismar.data.repository.anchorRepository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.DocumentReference
import es.itg.tourismar.data.model.anchor.Anchor
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.util.Resource
import kotlinx.coroutines.flow.Flow

interface AnchorRepository {

    fun createAnchorRoute(anchorRoute: AnchorRoute) : Flow<Resource<AnchorRoute>>
    fun readAnchorRoutes(): Flow<Resource<List<AnchorRoute>>>
    fun observeAnchorRoutes(): Flow<Resource<List<AnchorRoute>>>
    fun readAnchorRouteById(id: String): Flow<Resource<AnchorRoute?>>
    fun updateAnchorRoute(anchorRoute: AnchorRoute): Flow<Resource<Unit>>
    fun deleteAnchorRouteById(id: String): Flow<Resource<Unit>>
}