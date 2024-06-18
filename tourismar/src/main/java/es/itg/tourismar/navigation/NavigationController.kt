package es.itg.tourismar.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import es.itg.tourismar.data.model.anchor.AnchorRoute
import es.itg.tourismar.data.model.marker.MarkerRoute
import es.itg.tourismar.data.model.users.UserLevel
import es.itg.tourismar.ui.screens.arscreen.ARSceneScreen
import es.itg.tourismar.ui.screens.authentication.login.SignInScreen
import es.itg.tourismar.ui.screens.authentication.signup.SignUpScreen
import es.itg.tourismar.ui.screens.home.HomeScreen
import es.itg.tourismar.ui.screens.routesManagement.EditAnchorRouteScreen
import es.itg.tourismar.ui.screens.routesManagement.RoutesManagementScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationController() {
    val navController = rememberAnimatedNavController()
    val screens = listOf(
        Screens.Home,
        Screens.ARScene,
        Screens.SignIn,
        Screens.SignUp,
        Screens.RoutesManagement,
        Screens.EditAnchorRoute
    )

    var user by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
    var userLevel by remember { mutableStateOf<UserLevel?>(null) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        if (user != null) {
            scope.launch {
            userLevel = hasARSceneAccess()
            }
        }
        loading = false
        onDispose {  }
    }

    MyApp(navController = navController, userLevel = userLevel, onLogout = {
        FirebaseAuth.getInstance().signOut()
        user = null
        navController.navigate(Screens.SignIn.route) {
            popUpTo(0) { inclusive = true }
        }
    }) { paddings ->
        NavHost(navController = navController, startDestination = if (user == null) Screens.SignIn.route else Screens.Home.route) {
            screens.forEach { screen ->
                composable(
                    route = screen.route,
                    enterTransition = {
                        fadeIn(animationSpec = tween(700)) + slideInHorizontally(animationSpec = tween(1000))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(700)) + slideOutHorizontally(animationSpec = tween(1000))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(700)) + slideInHorizontally(animationSpec = tween(700), initialOffsetX = { -it })
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(700)) + slideOutHorizontally(animationSpec = tween(700), targetOffsetX = { -it })
                    }
                ) {
                    LaunchedEffect(user) {
                        scope.launch {
                            userLevel = hasARSceneAccess()
                        }
                    }
                    when (screen) {
                        is Screens.Home -> HomeScreen(navController)
                        is Screens.ARScene -> {
                            val anchorRoute: AnchorRoute? = navController.previousBackStackEntry?.savedStateHandle?.get<AnchorRoute>("anchorRoute")
                            val markerRoute: MarkerRoute? = navController.previousBackStackEntry?.savedStateHandle?.get<MarkerRoute>("markerRoute")
                            val level: UserLevel = navController.previousBackStackEntry?.savedStateHandle?.get<UserLevel>("userLevel") ?: UserLevel.NORMAL
                            ARSceneScreen(navController, anchorRoute, markerRoute, level)
                        }
                        is Screens.SignIn -> SignInScreen(navController)
                        is Screens.SignUp -> SignUpScreen(navController)
                        is Screens.RoutesManagement -> RoutesManagementScreen(navController)
                        is Screens.EditAnchorRoute -> {
                            val anchorRoute: AnchorRoute? = navController.previousBackStackEntry?.savedStateHandle?.get<AnchorRoute>("selectedRoute")
                            EditAnchorRouteScreen(anchorRoute, navController, Modifier)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MyApp(navController: NavController, userLevel: UserLevel?, onLogout: () -> Unit, content: @Composable (PaddingValues) -> Unit) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val showModalDrawer = currentRoute !in listOf(Screens.SignIn.route, Screens.ARScene.route, Screens.SignUp.route, Screens.EditAnchorRoute.route)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val items = listOf(
        NavigationItem(
            title = Screens.Home.route,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
        )
    )
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                items.forEachIndexed { index, item ->
                    NavigationDrawerItem(
                        label = { Text(text = item.title) },
                        selected = index == selectedItemIndex,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(item.title)
                            }
                            selectedItemIndex = index
                        },
                        icon = {
                            Icon(
                                imageVector = if (index == selectedItemIndex) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                Button(
                    onClick = {
                        scope.launch {
                            onLogout()
                            drawerState.close()
                            navController.navigate(Screens.SignIn.route)
                        }
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
                    if (showModalDrawer && userLevel == UserLevel.PRIVILEGED) {
                        NavigationBar(
                            modifier = Modifier.height(50.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ) {
                            NavigationBarItem(
                                selected = navController.currentDestination?.route == Screens.Home.route,
                                onClick = { navController.navigate(Screens.Home.route) },
                                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                            )
                            NavigationBarItem(
                                selected = navController.currentDestination?.route == Screens.RoutesManagement.route,
                                onClick = { navController.navigate(Screens.RoutesManagement.route) },
                                icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                            )
                        }
                    }
                }
            ) {
                content(it)
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

    if (querySnapshot.isEmpty) return UserLevel.NORMAL

    val documentSnapshot = querySnapshot.documents[0]
    return if (documentSnapshot.getString("userLevel") == UserLevel.PRIVILEGED.toString()) {
        UserLevel.PRIVILEGED
    } else {
        UserLevel.NORMAL
    }
}
