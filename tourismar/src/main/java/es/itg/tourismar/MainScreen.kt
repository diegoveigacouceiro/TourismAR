package es.itg.tourismar

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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
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
import es.itg.tourismar.navigation.NavigationItem
import es.itg.tourismar.navigation.Screens
import es.itg.tourismar.ui.screens.ARSceneScreen
import es.itg.tourismar.ui.screens.home.HomeScreen
import es.itg.tourismar.ui.screens.SettingsScreen
import es.itg.tourismar.ui.screens.authentication.login.SignInScreen
import es.itg.tourismar.ui.screens.authentication.signup.SignUpScreen
import kotlinx.coroutines.launch

@Composable
fun MyApp(
) {
    val navHostController = rememberNavController()

    val screens = listOf(
        Screens.Home,
        Screens.Settings,
        Screens.ARScene,
        Screens.SignIn,
        Screens.SignUp
    )

    NavHost(navController = navHostController, startDestination = Screens.SignIn.route) {
        screens.forEach { screen ->
            composable(screen.route) {
                MyApp2(navController = navHostController, onLogout = { FirebaseAuth.getInstance().signOut() }, currentScreen = screen)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp2(navController: NavController, onLogout: () -> Unit, currentScreen: Screens?) {
    var showModalDrawer by remember { mutableStateOf(currentScreen !in listOf(Screens.SignIn, Screens.ARScene, Screens.SignUp)) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
    var selectedItemIndex by rememberSaveable { mutableStateOf(0)}



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
                                selected = navController.currentDestination?.route == Screens.Settings.route,
                                onClick = { navController.navigate(Screens.Settings.route) },
                                icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                            )
                            NavigationBarItem(
                                selected = navController.currentDestination?.route == Screens.ARScene.route,
                                onClick = { navController.navigate(Screens.ARScene.route) },
                                icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                            )

                        }
                    }
                }
            ) {
                it
                when (currentScreen) {
                    is Screens.Home -> HomeScreen(navController,Modifier)
                    is Screens.Settings -> SettingsScreen()
                    is Screens.ARScene -> ARSceneScreen()
                    is Screens.SignIn -> SignInScreen(navController)
                    is Screens.SignUp -> SignUpScreen(navController)
                    null -> TODO()
                }
            }

        }
    )
}
