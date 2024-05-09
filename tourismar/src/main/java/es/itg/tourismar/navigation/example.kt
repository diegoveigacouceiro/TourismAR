package es.itg.tourismar.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

// Definir los destinos de la navegación
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Screen1 : Screen("screen1")
    object Screen2 : Screen("screen2")
}

// Modelo para los parámetros adicionales
data class AdditionalParams(val param1: String, val param2: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp() {
    val navController = rememberNavController()


    ModalDrawerSheet(
    ) {
        // Contenido principal
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Jetpack Compose Navigation") })
            },
            bottomBar = {
                BottomAppBar {
                    IconButton(onClick = {
                    }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Open Bottom Sheet")
                    }
                }
            }
        ) {
            it
            // Navigation host
            NavHost(navController = navController, startDestination = Screen.Home.route) {
                composable(Screen.Home.route) {
                    HomeScreen(navController = navController)
                }
                composable(Screen.Screen1.route) {
                    Screen1(navController = navController)
                }
                composable(route = Screen.Screen2.route + "?text={text}",
                    arguments = listOf(navArgument("text") {
                        type = NavType.StringType
                        nullable = true
                    })){
                    Screen2(navController = navController, param =it.arguments?.getString("text") )
                }



            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Home Screen")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate(Screen.Screen1.route) }) {
            Text("Go to Screen 1")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate(Screen.Screen2.route + "?text=value")
        }) {
            Text("Go to Screen 2")
        }
    }
}

@Composable
fun Screen1(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Screen 1")
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun Screen2(navController: NavHostController, param: String?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Screen 2")
        Spacer(modifier = Modifier.height(16.dp))
        param?.let {
            Text("Param: $it")

        }

    }
}



@Composable
fun MyAppWithBackHandler() {
    val navController = rememberNavController()

    // Handle back button presses
    BackHandler {
        if (navController.currentBackStackEntry?.destination?.route == Screen.Home.route) {
            // If we're on the home screen, close the app
            // For simplicity, you can handle app closing in your actual app
        } else {
            // Otherwise, navigate up in the navigation stack
            navController.navigateUp()
        }
    }

    MyApp()
}

