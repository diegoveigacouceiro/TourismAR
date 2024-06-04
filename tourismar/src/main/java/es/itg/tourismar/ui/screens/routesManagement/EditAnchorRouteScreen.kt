package es.itg.tourismar.ui.screens.routesManagement

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import es.itg.tourismar.data.model.anchor.AnchorRoute
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.itg.tourismar.R
import es.itg.tourismar.navigation.Screens
import es.itg.tourismar.ui.theme.SceneViewTheme


@Composable
fun EditAnchorRouteScreen(
    anchorRoute: AnchorRoute?,
    navController: NavController,
    viewModel: RoutesManagementViewModel = hiltViewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    SceneViewTheme(
        dynamicColor = true
    ) {
        Surface {
            if (anchorRoute != null) {
                EditAnchorRouteScreenContent(
                    anchorRoute,
                    navController,
                    viewModel
                ) { showDeleteDialog = true }

                if (showDeleteDialog) {

                    ShowDeleteRouteConfirmationDialog(
                        anchorRoute.id,
                        navController,
                        viewModel
                    ) { showDeleteDialog = false }
                }
            }
        }
    }
}

@Composable
fun EditAnchorRouteScreenContent(
    selectedRoute: AnchorRoute,
    navController: NavController,
    viewModel: RoutesManagementViewModel,
    onClickDelete: () -> Unit
) {
    val routeName = remember { mutableStateOf(selectedRoute.anchorRouteName) }
    val routeDescription = remember { mutableStateOf(selectedRoute.description) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextField(
            value = routeName.value,
            onValueChange = { routeName.value = it },
            label = { Text(stringResource(R.string.route_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = routeDescription.value,
            onValueChange = { routeDescription.value = it },
            label = { Text(stringResource(R.string.route_description)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ElevatedButton(
                onClick = {
                    // Update route with new data
                    viewModel.updateAnchorRoute(
                        selectedRoute.copy(
                            anchorRouteName = routeName.value,
                            description = routeDescription.value
                        )
                    )
                    navController.popBackStack()
                }
            ) {
                Text(text = stringResource(R.string.save))
            }
            ElevatedButton(
                onClick = onClickDelete,
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text(text = stringResource(R.string.delete))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FloatingActionButton(
            onClick = {
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "anchorRoute", selectedRoute
                )
                navController.navigate(Screens.ARScene.route)
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.ar_scene))
        }
    }
}

@Composable
fun ShowDeleteRouteConfirmationDialog(
    routeId: String,
    navController: NavController,
    viewModel: RoutesManagementViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_route_confirmation)) },
        text = { Text(stringResource(R.string.delete_route_message)) },
        confirmButton = {
            TextButton(onClick = {
                     viewModel.deleteAnchorRoute(routeId)
                    navController.popBackStack()

            }) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

