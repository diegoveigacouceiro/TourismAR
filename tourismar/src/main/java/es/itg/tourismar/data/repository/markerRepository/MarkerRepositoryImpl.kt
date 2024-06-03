package es.itg.tourismar.data.repository.markerRepository

import com.google.firebase.firestore.FirebaseFirestore
import es.itg.tourismar.data.model.marker.MarkerRoute
import es.itg.tourismar.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MarkerRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
): MarkerRepository {

    override fun readMarkerRoutes(): Flow<Resource<List<MarkerRoute>>> {
        return flow {
            emit(Resource.Loading())
            val result = firestore.collection("markersRoutes").get().await()
            val markerRoutes = result.documents.mapNotNull { document ->
                document.toObject(MarkerRoute::class.java)
            }
            emit(Resource.Success(markerRoutes))

        }.catch { e ->
            emit(Resource.Error(e.message ?: "An error occurred"))

        }
    }

    override fun observeMarkerRoutes(): Flow<Resource<List<MarkerRoute>>> {
        return callbackFlow<Resource<List<MarkerRoute>>> {
            val listener = firestore.collection("markersRoutes")
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        trySend(Resource.Error(exception.message ?: "An error occurred"))
                        return@addSnapshotListener
                    }

                    snapshot?.let { querySnapshot ->
                        val markersRoutes = mutableListOf<MarkerRoute>()
                        for (document in querySnapshot.documents) {
                            val markersRoute = document.toObject(MarkerRoute::class.java)
                            markersRoute?.let { markersRoutes.add(it) }
                        }
                        trySend(Resource.Success(markersRoutes))
                    }
                }
            awaitClose { listener.remove() }
        }
    }

    override fun readMarkerRouteById(id: String): Flow<Resource<MarkerRoute?>> {
        return flow {
            emit(Resource.Loading())
            val result = firestore.collection("markersRoutes").document(id).get().await()
            val markerRoute = result.toObject(MarkerRoute::class.java)
            emit(Resource.Success(markerRoute))
        }.catch {
            emit(Resource.Error(it.message.toString()))
        }
    }
}