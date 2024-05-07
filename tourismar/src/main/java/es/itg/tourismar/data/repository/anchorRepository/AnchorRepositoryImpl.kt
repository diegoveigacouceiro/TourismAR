package es.itg.tourismar.data.repository.anchorRepository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import es.itg.tourismar.data.model.anchor.Anchor
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject



class AnchorRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
): AnchorRepository {

    override fun createAnchorRoute(anchorRoute: AnchorRoute): Flow<Resource<Task<Void>>> {
        return flow {
            emit(Resource.Loading())
            val anchorRouteWithId = anchorRoute.copy(id = UUID.randomUUID().toString())
            val result = firestore.collection("anchorRoutes").document(anchorRouteWithId.id).set(anchorRouteWithId)
            emit(Resource.Success(result))
        }.catch {
            emit(Resource.Error(it.message.toString()))
        }
    }

    override fun readAnchorRoutes(): Flow<Resource<List<AnchorRoute>>> {
        return flow {
            emit(Resource.Loading())
            val result = firestore.collection("anchorRoutes").get().await()
            val anchorRoutes = result.documents.mapNotNull { document ->
                document.toObject(AnchorRoute::class.java)
            }
            emit(Resource.Success(anchorRoutes))

        }.catch { e ->
            emit(Resource.Error(e.message ?: "An error occurred"))

        }
    }

    // Por ejemplo, en tu repositorio:
    override fun observeAnchorRoutes(): Flow<Resource<List<AnchorRoute>>> {
        return callbackFlow<Resource<List<AnchorRoute>>> {
            val listener = firestore.collection("anchorRoutes")
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        // Manejar errores
                        trySend(Resource.Error(exception.message ?: "An error occurred"))
                        return@addSnapshotListener
                    }

                    snapshot?.let { querySnapshot ->
                        val anchorRoutes = mutableListOf<AnchorRoute>()
                        for (document in querySnapshot.documents) {
                            val anchorRoute = document.toObject(AnchorRoute::class.java)
                            anchorRoute?.let { anchorRoutes.add(it) }
                        }
                        trySend(Resource.Success(anchorRoutes))
                    }
                }

            awaitClose { listener.remove() }
        }
    }









    override fun readAnchorRouteById(id: String): Flow<Resource<AnchorRoute?>> {
        return flow {
            emit(Resource.Loading())
            val result = firestore.collection("anchorRoutes").document(id).get().await()
            val anchorRoute = result.toObject(AnchorRoute::class.java)
            emit(Resource.Success(anchorRoute))
        }.catch {
            emit(Resource.Error(it.message.toString()))
        }
    }

    override fun updateAnchorRoute(anchorRoute: AnchorRoute): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())
            firestore.collection("anchorRoutes").document(anchorRoute.id).set(anchorRoute).await()
            emit(Resource.Success(Unit))
        }.catch {
            emit(Resource.Error(it.message.toString()))
        }
    }

    override fun deleteAnchorRouteById(id: String): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading())
            firestore.collection("anchorRoutes").document(id).delete().await()
            emit(Resource.Success(Unit))
        }.catch {
            emit(Resource.Error(it.message.toString()))
        }
    }
}