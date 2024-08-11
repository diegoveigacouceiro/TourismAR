package es.itg.tourismar.util

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun RequestMultiplePermissionsComposable(
    permissions: Array<String>,
    onPermissionsGranted: @Composable () -> Unit
) {
    var permissionsGranted by rememberSaveable { mutableStateOf(false) }
    val launcherMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionsGranted = result.values.fold(true) { acc, next -> acc && next }
    }

    LaunchedEffect(Unit) {
        launcherMultiplePermissions.launch(permissions)
    }

    if (permissionsGranted) {
        onPermissionsGranted()
    }
}
