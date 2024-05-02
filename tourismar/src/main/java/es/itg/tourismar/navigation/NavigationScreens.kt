package es.itg.tourismar.navigation


import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import es.itg.tourismar.ui.screens.ARSceneScreen
import es.itg.tourismar.ui.screens.HomeScreen
import es.itg.tourismar.ui.screens.SettingsScreen
import es.itg.tourismar.ui.screens.authentication.login.SignInScreen
import es.itg.tourismar.ui.screens.authentication.signup.SignUpScreen
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import es.itg.tourismar.ui.theme.SceneViewTheme
import kotlinx.coroutines.launch
@Composable
@Preview
fun navegación() {
    val navController = rememberNavController()
    val currentScreen = remember { mutableStateOf<Screens?>(null) }

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
                currentScreen.value = screen
                ModalNavigator(navController, currentScreen.value) {  } // Pass navController to onLogoutClick
                // Uncomment DestinationScreen
//                DestinationScreen(screen = screen, navController = navController, currentScreen.value) {
                    // Cerrar sesión (can be empty for now)
//                }
            }
        }
    }
}

data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int? = null

)
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ModalNavigator(navController: NavController, currentScreen: Screens?, onLogout: () -> Unit) {
    SceneViewTheme {
        val items = listOf(
            NavigationItem(
                title = Screens.Home.route,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
            )
        )

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var selectedItemIndex by rememberSaveable {
            mutableStateOf(0)
        }

        var drawerEnabled by remember { mutableStateOf(true) } // Controla si el cajón de navegación está habilitado o no

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
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
                    // Agregar el botón de cierre de sesión
                    Button(
                        onClick = {
                            onLogout()
                            navController.navigate(Screens.ARScene.route)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = "Cerrar sesión")
                    }
                }
            },
            gesturesEnabled = drawerEnabled, // Controla si el cajón de navegación está habilitado o no
            content = {
                // Place the actual screen content here (Scaffold)
                Scaffold(
                    // ... (TopAppBar definition)
                    content = { innerPadding ->
                        Box(
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            when (currentScreen) {
                                Screens.Home -> HomeScreen() // Replace with actual content
                                Screens.ARScene -> {
                                    drawerEnabled = false // Deshabilitar el cajón de navegación cuando se está en la pantalla de AR
                                    ARSceneScreen() // Replace with actual content
                                }
                                Screens.SignIn -> SignInScreen(navController)
                                else -> HomeScreen() // Handle other routes
                            }
                        }
                    }
                )
            }
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
fun DestinationScreen(screen: Screens, navController: NavController, currentScreen: Screens?, onLogout: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Todo App")
                }
            )
        },
        bottomBar = {
            // Barra de navegación inferior si la pantalla actual es diferente de la pantalla de inicio
            if (currentScreen != Screens.Home) {
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
        // Mostrar el contenido de la pantalla actual
        when (screen) {
            is Screens.Home -> HomeScreen()
            is Screens.Settings -> SettingsScreen()
            is Screens.ARScene -> ARSceneScreen()
            is Screens.SignIn -> SignInScreen(navController)
            is Screens.SignUp -> SignUpScreen(navController)
        }
    }
}

@Composable
fun onLogoutClick(navController: NavController) {
    // Perform logout logic here (e.g., clear user data)

    // Navigate to the desired screen
    navController.navigate(Screens.ARScene.route) // Replace with your desired screen route
}
