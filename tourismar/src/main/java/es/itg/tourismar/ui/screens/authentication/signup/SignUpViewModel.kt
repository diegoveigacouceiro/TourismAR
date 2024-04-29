package es.itg.tourismar.ui.screens.authentication.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itg.tourismar.data.repository.authRepository.UserAuthRepository
import es.itg.tourismar.ui.screens.authentication.login.SignInState
import es.itg.tourismar.util.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel

class SignUpViewModel @Inject constructor(
    private val repository: UserAuthRepository
) : ViewModel() {

    val _signUpState = Channel<SignUpState>()
    val signUpState = _signUpState.receiveAsFlow()


    fun registerUser(email:String, pasword:String) = viewModelScope.launch {
        repository.registerUser(email,pasword).collect{result ->
            when(result){
                is Resource.Success ->{
                    _signUpState.send(SignUpState(isSuccess = "Sing Up Success"))
                }

                is Resource.Loading -> {
                    _signUpState.send(SignUpState(isLoading = true))
                }

                is Resource.Error -> {
                    _signUpState.send(SignUpState(isError = result.message))
                }
            }
        }
    }
}