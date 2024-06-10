package es.itg.tourismar.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.data.model.marker.MarkerRoute
import es.itg.tourismar.data.model.users.UserLevel
import es.itg.tourismar.ui.screens.arscreen.ARSceneScreen
import es.itg.tourismar.ui.screens.authentication.login.SignInScreen
import es.itg.tourismar.ui.screens.authentication.signup.SignUpScreen
import es.itg.tourismar.ui.screens.home.HomeScreen
import es.itg.tourismar.ui.screens.markerScreen.MarkerScreen
import es.itg.tourismar.ui.screens.routesManagement.EditAnchorRouteScreen
import es.itg.tourismar.ui.screens.routesManagement.RoutesManagementScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun NavigationController() {
    val navController = rememberNavController()
    val screens = listOf(
        Screens.Home,
        Screens.MarkerScreen,
        Screens.ARScene,
        Screens.SignIn,
        Screens.SignUp,
        Screens.RoutesManagement,
        Screens.EditAnchorRoute
    )

    NavHost(navController = navController, startDestination = Screens.Home.route) {
        screens.forEach { screen ->
            when (screen) {
                is Screens.Home -> {
                    composable(route = screen.route) {
                        HomeScreen(navController)
                        MyApp(navController, onLogout = { FirebaseAuth.getInstance().signOut() }, currentScreen = screen)
                    }
                }
                is Screens.MarkerScreen -> {
                    composable(route = screen.route) {
                        MarkerScreen(navController)
                        MyApp(navController, onLogout = { FirebaseAuth.getInstance().signOut() }, currentScreen = screen)
                    }
                }
                is Screens.ARScene -> {
                    composable(route = screen.route) {
                        val anchorRoute: AnchorRoute? = navController.previousBackStackEntry?.savedStateHandle?.get("anchorRoute")
                        val markerRoute: MarkerRoute? = navController.previousBackStackEntry?.savedStateHandle?.get("markerRoute")
                        ARSceneScreen(navController, anchorRoute = anchorRoute, markerRoute = markerRoute)
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
                is Screens.RoutesManagement -> {
                    composable(route = screen.route) {
                        RoutesManagementScreen(navController)
                        MyApp(navController, onLogout = { FirebaseAuth.getInstance().signOut() }, currentScreen = screen)
                    }
                }
                is Screens.EditAnchorRoute -> {
                    composable(route = screen.route) {
                        val anchorRoute: AnchorRoute? = navController.previousBackStackEntry?.savedStateHandle?.get("selectedRoute")
                        EditAnchorRouteScreen(anchorRoute,navController,Modifier)
                    }
                }
            }
        }
    }
}



@Composable
fun MyApp(navController: NavController, onLogout: () -> Unit, currentScreen: Screens?) {
    val showModalDrawer by remember { mutableStateOf(currentScreen !in listOf(Screens.SignIn, Screens.ARScene, Screens.SignUp)) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var userLevel by remember { mutableIntStateOf(UserLevel.NORMAL.ordinal)}
    var levelcomprobation by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = levelcomprobation) {
        scope.launch {
            userLevel = hasARSceneAccess().ordinal
            levelcomprobation = true
        }
    }

    val items = listOf(
        NavigationItem(
            title = Screens.Home.route,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
        ),
        NavigationItem(
            title = Screens.ARScene.route,
            selectedIcon = Icons.Filled.PlayArrow,
            unselectedIcon = Icons.Outlined.PlayArrow,
        )
    )
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }



    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet() {
                Spacer(modifier = Modifier.height(16.dp))
                items.forEachIndexed { index, item ->
                    NavigationDrawerItem(
                        label = {
                            Text(text = item.title)
                        },
                        selected = index == selectedItemIndex,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                            }
                            navController.navigate(item.title)
                            selectedItemIndex = index
                        },
                        icon = {
                            Icon(
                                imageVector = if (index == selectedItemIndex) {
                                    item.selectedIcon
                                } else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                Button(
                    onClick = {
                        onLogout()
                        navController.navigate(Screens.SignIn.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "Cerrar sesión")
                }
            }
        },
        drawerState = drawerState,
        gesturesEnabled = showModalDrawer,
        content = {
            Scaffold(
                bottomBar = {
                    if (currentScreen !in listOf(Screens.SignIn, Screens.ARScene, Screens.SignUp)) {
                        NavigationBar(
                            modifier = Modifier
                                .height(50.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ) {
                            NavigationBarItem(
                                selected = navController.currentDestination?.route == Screens.Home.route,
                                onClick = { navController.navigate(Screens.Home.route) },
                                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                            )
                            NavigationBarItem(
                                selected = navController.currentDestination?.route == Screens.MarkerScreen.route,
                                onClick = { navController.navigate(Screens.MarkerScreen.route) },
                                icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                            )
                            if(userLevel == UserLevel.PRIVILEGED.ordinal){
                                NavigationBarItem(
                                    selected = navController.currentDestination?.route == Screens.RoutesManagement.route,
                                    onClick = { navController.navigate(Screens.RoutesManagement.route) },
                                    icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                                )
                            }
                        }
                    }
                }
            ) {
                it
                when (currentScreen) {
                    is Screens.Home -> HomeScreen(navController, Modifier)
                    is Screens.MarkerScreen -> MarkerScreen(navController)
                    is Screens.ARScene -> ARSceneScreen(navController,null,null)
                    is Screens.SignIn -> SignInScreen(navController)
                    is Screens.SignUp -> SignUpScreen(navController)
                    is Screens.RoutesManagement -> RoutesManagementScreen(navController)
                    is Screens.EditAnchorRoute -> EditAnchorRouteScreen(null, navController,Modifier)
                    null -> TODO()

                }
            }

        }
    )
}


suspend fun hasARSceneAccess(): UserLevel {
    val user = FirebaseAuth.getInstance().currentUser ?: return UserLevel.NORMAL
    val userId = user.uid
    val querySnapshot = FirebaseFirestore.getInstance()
        .collection("users")
        .whereEqualTo("userId", userId)
        .get()
        .await()

    if (querySnapshot.isEmpty) { return UserLevel.NORMAL }

    val documentSnapshot = querySnapshot.documents[0]
    return if(documentSnapshot.getString("userLevel") == UserLevel.PRIVILEGED.toString() ){
        UserLevel.PRIVILEGED
    }else{
        UserLevel.NORMAL
    }
}




