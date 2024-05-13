package es.itg.tourismar.ui.screens.arscreen

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itg.tourismar.data.repository.anchorRepository.AnchorRepository
import javax.inject.Inject

@HiltViewModel
class ARSceneViewModel @Inject constructor(
    repository: AnchorRepository
) : ViewModel() {


    init {
        repository.observeAnchorRoutes()
    }


    fun getdata(){
        Log.d("viewmodel","conectado")
    }
}