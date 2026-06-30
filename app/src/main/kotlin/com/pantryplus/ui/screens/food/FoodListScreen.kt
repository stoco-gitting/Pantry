package com.pantryplus.ui.screens.food

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
import com.pantryplus.data.db.entity.TrackingType
import com.pantryplus.data.db.entity.UsedStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodListScreen(
    vm: FoodViewModel,
    onAddManual: () -> Unit,
    onScan: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val entries by vm.foodEntries.collectAsState()
    val query by vm.query.collectAsState()
    var entryToDelete by remember { mutableStateOf<FoodEntry?>(null) }
    var showAddMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Food Inventory") })
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { showAddMenu = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
                DropdownMenu(expanded = showAddMenu, onDismissRequest = { showAddMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Scan barcode") },
                        leadingIcon = { Icon(Icons.Default.QrCodeScanner, null) },
                        onClick = { showAddMenu = false; onScan() }
                    )
                    DropdownMenuItem(
                        text = { Text("Add manually") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = { showAddMenu = false; onAddManual() }
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.query.value = it },
                placeholder = { Text("Search by name, category, location…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No food items. Tap + to add one.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn {
                    items(entries, key = { it.instance.id }) { entry ->
                        FoodItem(
                            entry = entry,
                            onEdit = { onEdit(entry.instance.id) },
                            onDelete = { entryToDelete = entry },
                            onToggleUsed = {
                                val updated = entry.instance.copy(
                                    usedStatus = if (entry.instance.usedStatus == UsedStatus.USED) UsedStatus.UNUSED else UsedStatus.USED
                                )
                                vm.upsertInstance(updated)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Remove item?") },
            text = { Text("Remove this instance of \"${entry.product.name}\"?") },
            confirmButton = {
                TextButton(onClick = { vm.deleteInstance(entry.instance); entryToDelete = null }) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun FoodItem(
    entry: FoodEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleUsed: () -> Unit
) {
    val inst = entry.instance
    val product = entry.product
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    val subtitle = buildString {
        append(inst.location)
        inst.expiryDate?.let { append(" · Exp: ${dateFormat.format(Date(it))}") }
        if (product.trackingType == TrackingType.WEIGHT_VOLUME) {
            val remaining = inst.remainingAmount
            val starting = inst.startingAmount
            val unit = inst.unit ?: ""
            if (remaining != null) append(" · ${remaining}${unit} left")
            else if (starting != null) append(" · ${starting}${unit}")
        }
    }

    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(product.name)
                product.category?.let {
                    Spacer(Modifier.width(8.dp))
                    AssistChip(onClick = {}, label = { Text(it) })
                }
            }
        },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (product.trackingType == TrackingType.DISCRETE) {
                    Checkbox(
                        checked = inst.usedStatus == UsedStatus.USED,
                        onCheckedChange = { onToggleUsed() }
                    )
                }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete") }
            }
        }
    )
}
