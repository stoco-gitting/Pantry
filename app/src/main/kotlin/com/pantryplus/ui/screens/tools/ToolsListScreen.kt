package com.pantryplus.ui.screens.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantryplus.data.db.entity.Tool

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsListScreen(
    vm: ToolsViewModel,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onSettings: () -> Unit
) {
    val tools by vm.tools.collectAsState()
    val query by vm.query.collectAsState()
    var toolToDelete by remember { mutableStateOf<Tool?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tools") },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add Tool")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.query.value = it },
                placeholder = { Text("Search tools…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (tools.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tools yet. Tap + to add one.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn {
                    items(tools, key = { it.id }) { tool ->
                        ToolItem(
                            tool = tool,
                            onEdit = { onEdit(tool.id) },
                            onDelete = { toolToDelete = tool }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    toolToDelete?.let { tool ->
        AlertDialog(
            onDismissRequest = { toolToDelete = null },
            title = { Text("Delete tool?") },
            text = { Text("\"${tool.name}\" will be removed.") },
            confirmButton = {
                TextButton(onClick = { vm.delete(tool); toolToDelete = null }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { toolToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ToolItem(tool: Tool, onEdit: () -> Unit, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(tool.name) },
        supportingContent = { Text(tool.location) },
        trailingContent = {
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    )
}
