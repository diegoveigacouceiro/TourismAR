package es.itg.tourismar.navigation

sealed class Screens(val route: String) {
    object Home : Screens("home")
    object ARScene : Screens("arScene")
    object SignIn : Screens("sign in")
    object SignUp : Screens("sign up")
    object RoutesManagement : Screens("routesManagement")

    object EditAnchorRoute : Screens("editAnchorRoute")
}