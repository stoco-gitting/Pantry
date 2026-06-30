package com.pantryplus.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val event by vm.event.collectAsState()
    var showImportConfirm by remember { mutableStateOf<Uri?>(null) }
    var snackMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { showImportConfirm = it } }

    LaunchedEffect(event) {
        when (val e = event) {
            is SettingsEvent.ShareFile -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, e.uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share export file"))
                vm.clearEvent()
            }
            is SettingsEvent.Message -> {
                snackbarHostState.showSnackbar(e.text)
                vm.clearEvent()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Data", style = MaterialTheme.typography.titleMedium)

            Button(
                onClick = { vm.export(context) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export to JSON")
            }

            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import from JSON")
            }

            Text(
                "Export saves a JSON file you can share via Bluetooth, USB, or messaging. " +
                "Import fully replaces all current data with the selected file.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    showImportConfirm?.let { uri ->
        AlertDialog(
            onDismissRequest = { showImportConfirm = null },
            title = { Text("Replace all data?") },
            text = { Text("This will delete all current products, food items, and tools and replace them with the imported file. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.import(context, uri)
                    showImportConfirm = null
                }) { Text("Replace") }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirm = null }) { Text("Cancel") }
            }
        )
    }
}
