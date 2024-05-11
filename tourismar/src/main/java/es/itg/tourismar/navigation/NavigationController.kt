package es.itg.tourismar.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import es.itg.tourismar.MyApp2
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.ui.screens.arscreen.ARSceneScreen
import es.itg.tourismar.ui.screens.SettingsScreen
import es.itg.tourismar.ui.screens.authentication.login.SignInScreen
import es.itg.tourismar.ui.screens.authentication.signup.SignUpScreen
import es.itg.tourismar.ui.screens.home.HomeScreen

@Composable
fun NavigationController() {
    val navController = rememberNavController()
    val screens = listOf(
        Screens.Home,
        Screens.Settings,
        Screens.ARScene,
        Screens.SignIn,
        Screens.SignUp
    )

    NavHost(navController = navController, startDestination = Screens.ARScene.route) {
        screens.forEach { screen ->
            when (screen) {
                is Screens.Home -> {
                    composable(route = screen.route) {
                        HomeScreen(navController)
                        MyApp2(navController, onLogout = {FirebaseAuth.getInstance().signOut()}, currentScreen = screen)

                    }
                }
                is Screens.Settings -> {
                    composable(route = screen.route) {
                        SettingsScreen()
                        MyApp2(navController, onLogout = {FirebaseAuth.getInstance().signOut()}, currentScreen = screen)
                    }
                }
                is Screens.ARScene -> {
                    composable(route = screen.route) {
                        val anchorRoute: AnchorRoute? = navController.previousBackStackEntry?.savedStateHandle?.get("anchorRoute")
                        ARSceneScreen(navController, anchorRoute = anchorRoute)
                    }


                }
                is Screens.SignIn -> {
                    composable(route = screen.route) {
                        SignInScreen(navController)
                    }
                }
                is Screens.SignUp -> {
                    composable(route = screen.route) {
                        SignUpScreen(navController)
                    }
                }

            }
        }
    }
}




