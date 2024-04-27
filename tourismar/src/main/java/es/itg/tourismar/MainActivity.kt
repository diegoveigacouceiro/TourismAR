package es.itg.tourismar

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import es.itg.tourismar.ui.screens.ARSceneScreen
import es.itg.tourismar.ui.screens.HomeScreen
import es.itg.tourismar.ui.screens.SettingsScreen
import es.itg.tourismar.ui.screens.authentication.login.SignInScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object ARScene : Screen("arScene")
    // Agrega más pantallas según sea necesario
    object Login : Screen("login")
}

@Composable
@Preview
fun MyApp() {
    val navController = rememberNavController()

    val screens = listOf(
        Screen.Home,
        Screen.Settings,
        Screen.ARScene,
        Screen.Login
    )

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        screens.forEach { screen ->
            composable(screen.route) {
                DestinationScreen(screen = screen, navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun DestinationScreen(screen: Screen, navController: NavController) {
    val title = when (screen) {
        is Screen.Home -> "Home"
        is Screen.Settings -> "Settings"
        is Screen.ARScene -> "AR Scene"
        is Screen.Login -> "Login "
    }

    Scaffold(
    ) {
        it
        Box(modifier = Modifier.fillMaxSize()) {
            when (screen) {
                is Screen.Home -> HomeScreen()
                is Screen.Settings -> SettingsScreen()
                is Screen.ARScene -> ARSceneScreen()
                is Screen.Login -> SignInScreen()

            }

            // Bottom navigation
            NavigationBar(
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                NavigationBarItem(
                    selected = navController.currentDestination?.route == Screen.Home.route,
                    onClick = { navController.navigate(Screen.Home.route) },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = navController.currentDestination?.route == Screen.Settings.route,
                    onClick = { navController.navigate(Screen.Settings.route) },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
                NavigationBarItem(
                    selected = navController.currentDestination?.route == Screen.ARScene.route,
                    onClick = { navController.navigate(Screen.ARScene.route) },
                    icon = { Icon(Icons.Filled.PlayArrow, contentDescription = "AR Scene") },
                    label = { Text("AR Scene") }
                )
            }
        }
    }
}