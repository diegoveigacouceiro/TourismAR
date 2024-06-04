package es.itg.tourismar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import es.itg.tourismar.navigation.NavigationController
import es.itg.tourismar.ui.theme.SceneViewTheme


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SceneViewTheme(
                dynamicColor = true,

            ) {
                NavigationController()
            }
        }
    }
}







