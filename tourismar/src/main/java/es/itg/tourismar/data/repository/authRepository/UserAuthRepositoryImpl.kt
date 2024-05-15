package es.itg.tourismar.data.repository.authRepository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import es.itg.tourismar.data.model.users.User
import es.itg.tourismar.data.model.users.UserLevel
import es.itg.tourismar.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
): UserAuthRepository {

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser!= null
    }
    override fun loginUser(email: String, password: String): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            val result = firebaseAuth.signInWithEmailAndPassword(email,password).await()
            emit(Resource.Success(result))
        }.catch{
            emit(Resource.Error(it.message.toString()))
        }
    }

    override fun registerUser(email: String, password: String): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            val result = firebaseAuth.createUserWithEmailAndPassword(email,password).await()
            emit(Resource.Success(result))
        }.catch{
            emit(Resource.Error(it.message.toString()))
        }
    }

    override fun googleSignIn(credential: AuthCredential): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            val result = firebaseAuth.signInWithCredential(credential).await()
            emit(Resource.Success(result))
        }.catch {
            emit(Resource.Error(it.message.toString()))
        }
    }

    override fun createUser(
        email: String
    ): Flow<Resource<DocumentReference>> {
        val name = email.substringBefore("@")
        val uid = firebaseAuth.currentUser?.uid
        val user = User(
            userName = name,
            userId = uid.toString(),
            email = email,
            userLevel = UserLevel.NORMAL
        )
        return flow {
            emit(Resource.Loading())
            val documentReference = firebaseFirestore.collection("users").add(user).await()
            emit(Resource.Success(documentReference))
        }.catch{
            emit(Resource.Error(it.message.toString()))
        }


    }
}