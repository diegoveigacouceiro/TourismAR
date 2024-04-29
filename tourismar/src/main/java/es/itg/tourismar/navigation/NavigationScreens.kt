package es.itg.tourismar.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun NavigationDrawer(navController: NavController) {
    val screens = listOf(Screens.Home, Screens.Settings, Screens.ARScene)

    Scaffold(
    ) {
        it
        Box(modifier = Modifier.fillMaxSize()) {
            // Your screen content here (Replace with appropriate composable calls)
            val currentDestination = navController.currentBackStackEntry?.destination?.route

            // Bottom navigation
            NavigationBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                screens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentDestination == screen.route,
                        onClick = { navController.navigate(screen.route) },
                        icon = {
                            when (screen) {
                                Screens.Home -> Icon(Icons.Filled.Home, contentDescription = "Home")
                                Screens.Settings -> Icon(Icons.Filled.Settings, contentDescription = "Settings")
                                Screens.ARScene -> Icon(Icons.Filled.PlayArrow, contentDescription = "AR Scene")
                                else -> {}
                            }
                        },
                        label = { Text(screen.route.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavigationDrawerPreview() {
    val navController = rememberNavController()
    NavigationDrawer(navController)
}
