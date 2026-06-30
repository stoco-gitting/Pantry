package com.pantryplus.ui.screens.scanner.util

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun rememberPermissionState(permission: String): State<Boolean> {
    val context = LocalContext.current
    val granted = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> granted.value = isGranted }

    LaunchedEffect(permission) {
        if (!granted.value) launcher.launch(permission)
    }

    return granted
}
