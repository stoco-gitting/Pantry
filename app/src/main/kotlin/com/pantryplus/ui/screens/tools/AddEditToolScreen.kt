package com.pantryplus.ui.screens.tools

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantryplus.data.db.entity.Tool

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditToolScreen(vm: ToolsViewModel, toolId: Long?, onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var existing by remember { mutableStateOf<Tool?>(null) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(toolId) {
        if (toolId != null) {
            existing = vm.getById(toolId)
            name = existing?.name ?: ""
            location = existing?.location ?: ""
        }
        loaded = true
    }

    val isEdit = toolId != null
    val canSave = name.isNotBlank() && location.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Tool" else "Add Tool") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (!loaded) return@Scaffold

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location *") },
                placeholder = { Text("e.g. Garage, Kitchen, Shed") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val tool = Tool(
                        id = existing?.id ?: 0L,
                        name = name.trim(),
                        location = location.trim(),
                        dateAdded = existing?.dateAdded ?: System.currentTimeMillis()
                    )
                    vm.upsert(tool)
                    onDone()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEdit) "Save Changes" else "Add Tool")
            }
        }
    }
}
