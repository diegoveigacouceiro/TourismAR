package es.itg.tourismar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itg.tourismar.data.repository.authRepository.UserAuthRepository
import es.itg.tourismar.navigation.Screens
import javax.inject.Inject


