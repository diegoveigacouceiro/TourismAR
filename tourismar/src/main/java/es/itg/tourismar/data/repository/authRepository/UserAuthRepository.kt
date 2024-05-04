package es.itg.tourismar.data.repository.authRepository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.firestore.DocumentReference
import es.itg.tourismar.util.Resource
import kotlinx.coroutines.flow.Flow

interface UserAuthRepository {

    fun loginUser(email : String, password: String): Flow<Resource<AuthResult>>
    fun registerUser(email: String, password: String):Flow<Resource<AuthResult>>
    fun googleSignIn(credential: AuthCredential): Flow<Resource<AuthResult>>
    fun createUser(email: String): Flow<Resource<DocumentReference>>
    fun isUserLoggedIn(): Boolean
}