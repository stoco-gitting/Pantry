package com.pantryplus.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pantryplus.data.db.entity.Tool
import com.pantryplus.ui.screens.food.FoodEntry
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(vm: DashboardViewModel, onNavigateToFood: () -> Unit) {
    val expiringSoon by vm.expiringSoon.collectAsState()
    val tools by vm.tools.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Scaffold(topBar = { TopAppBar(title = { Text("Pantry+") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader("Expiring Soon (next 14 days)")
            }
            if (expiringSoon.isEmpty()) {
                item { Text("Nothing expiring soon.", style = MaterialTheme.typography.bodyMedium) }
            } else {
                items(expiringSoon) { entry ->
                    ExpiryCard(entry, dateFormat)
                }
            }

            item { HorizontalDivider() }

            item { SectionHeader("Tools") }
            if (tools.isEmpty()) {
                item { Text("No tools added yet.", style = MaterialTheme.typography.bodyMedium) }
            } else {
                items(tools) { tool ->
                    ToolCard(tool)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
private fun ExpiryCard(entry: FoodEntry, dateFormat: SimpleDateFormat) {
    val expiry = entry.instance.expiryDate?.let { dateFormat.format(Date(it)) } ?: "Unknown"
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(entry.product.name, style = MaterialTheme.typography.bodyLarge)
            Text("${entry.instance.location} · Expires $expiry", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ToolCard(tool: Tool) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(tool.name, style = MaterialTheme.typography.bodyLarge)
            Text(tool.location, style = MaterialTheme.typography.bodySmall)
        }
    }
}
