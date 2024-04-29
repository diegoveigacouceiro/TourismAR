package es.itg.tourismar.navigation

sealed class Screens(val route: String) {
    object Home : Screens("home")
    object Settings : Screens("settings")
    object ARScene : Screens("arScene")
    object SignIn : Screens("sign in")
    object SignUp : Screens("sign up")
}