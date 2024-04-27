package es.itg.tourismar.ui.screens.authentication.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itg.tourismar.data.repository.authRepository.UserAuthRepository
import es.itg.tourismar.util.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel

class SignInViewModel @Inject constructor(
    private val repository: UserAuthRepository
) : ViewModel() {

    val _signInState = Channel<SignInState>()
    val signInState = _signInState.receiveAsFlow()


    fun loginUser(email:String, pasword:String) = viewModelScope.launch {
        repository.loginUser(email,pasword).collect{result ->
            when(result){
                is Resource.Success ->{
                    _signInState.send(SignInState(isSuccess = "Sing In Success"))
                }

                is Resource.Loading -> {
                    _signInState.send(SignInState(isLoading = true))
                }

                is Resource.Error -> {
                    _signInState.send(SignInState(isError = result.message))
                }
            }
        }
    }


}