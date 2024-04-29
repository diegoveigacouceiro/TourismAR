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
import androidx.compose.material3.MaterialTheme
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
import es.itg.tourismar.navigation.Screens
import es.itg.tourismar.ui.screens.ARSceneScreen
import es.itg.tourismar.ui.screens.HomeScreen
import es.itg.tourismar.ui.screens.SettingsScreen
import es.itg.tourismar.ui.screens.authentication.login.SignInScreen
import es.itg.tourismar.ui.screens.authentication.signup.SignUpScreen
import es.itg.tourismar.ui.screens.authentication.signup.SignUpViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}


@Composable
@Preview
fun MyApp() {
    val navController = rememberNavController()

    val screens = listOf(
        Screens.Home,
        Screens.Settings,
        Screens.ARScene,
        Screens.SignIn,
        Screens.SignUp
    )

    NavHost(navController = navController, startDestination = Screens.SignIn.route) {
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
fun DestinationScreen(screen: Screens, navController: NavController) {

    Scaffold(
    ) {
        it
        Box(modifier = Modifier.fillMaxSize()) {
            when (screen) {
                is Screens.Home -> HomeScreen()
                is Screens.Settings -> SettingsScreen()
                is Screens.ARScene -> ARSceneScreen()
                is Screens.SignIn -> SignInScreen(navController)
                is Screens.SignUp -> SignUpScreen(navController)
            }

            // Bottom navigation
            NavigationBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                NavigationBarItem(
                    selected = navController.currentDestination?.route == Screens.Home.route,
                    onClick = { navController.navigate(Screens.Home.route) },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = navController.currentDestination?.route == Screens.Settings.route,
                    onClick = { navController.navigate(Screens.Settings.route) },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
                NavigationBarItem(
                    selected = navController.currentDestination?.route == Screens.ARScene.route,
                    onClick = { navController.navigate(Screens.ARScene.route) },
                    icon = { Icon(Icons.Filled.PlayArrow, contentDescription = "AR Scene") },
                    label = { Text("AR Scene") }
                )
            }
        }
    }
}