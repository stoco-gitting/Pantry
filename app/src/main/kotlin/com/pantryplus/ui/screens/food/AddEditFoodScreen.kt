package com.pantryplus.ui.screens.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pantryplus.data.db.entity.FoodInstance
import com.pantryplus.data.db.entity.Product
import com.pantryplus.data.db.entity.TrackingType
import com.pantryplus.data.db.entity.UsedStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFoodScreen(
    vm: FoodViewModel,
    instanceId: Long?,
    prefillBarcode: String?,
    prefillProductId: Long?,
    onDone: () -> Unit
) {
    val isEdit = instanceId != null

    // Product fields
    var productId by remember { mutableStateOf<Long?>(prefillProductId) }
    var barcode by remember { mutableStateOf(prefillBarcode ?: "") }
    var productName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var trackingType by remember { mutableStateOf(TrackingType.DISCRETE) }
    var existingProduct by remember { mutableStateOf<Product?>(null) }

    // Instance fields
    var startingAmount by remember { mutableStateOf("") }
    var remainingAmount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var expiryDateText by remember { mutableStateOf("") }
    var usedStatus by remember { mutableStateOf(UsedStatus.UNUSED) }
    var existingInstance by remember { mutableStateOf<FoodInstance?>(null) }

    var productResolved by remember { mutableStateOf(false) }
    var showUseDialog by remember { mutableStateOf(false) }
    var amountUsed by remember { mutableStateOf("") }

    LaunchedEffect(prefillBarcode, prefillProductId, instanceId) {
        when {
            instanceId != null -> {
                val inst = vm.getInstanceById(instanceId)
                existingInstance = inst
                if (inst != null) {
                    location = inst.location
                    expiryDateText = inst.expiryDate?.let {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
                    } ?: ""
                    startingAmount = inst.startingAmount?.toString() ?: ""
                    remainingAmount = inst.remainingAmount?.toString() ?: ""
                    unit = inst.unit ?: ""
                    usedStatus = inst.usedStatus ?: UsedStatus.UNUSED
                    val p = vm.getProductById(inst.productId)
                    existingProduct = p
                    productId = p?.id
                    productName = p?.name ?: ""
                    category = p?.category ?: ""
                    trackingType = p?.trackingType ?: TrackingType.DISCRETE
                    barcode = p?.barcode ?: ""
                }
            }
            prefillProductId != null -> {
                val p = vm.getProductById(prefillProductId)
                existingProduct = p
                productId = p?.id
                productName = p?.name ?: ""
                category = p?.category ?: ""
                trackingType = p?.trackingType ?: TrackingType.DISCRETE
                barcode = p?.barcode ?: ""
            }
            prefillBarcode != null -> {
                val p = vm.getByBarcode(prefillBarcode)
                if (p != null) {
                    existingProduct = p
                    productId = p.id
                    productName = p.name
                    category = p.category ?: ""
                    trackingType = p.trackingType
                    barcode = p.barcode ?: prefillBarcode
                }
            }
        }
        productResolved = true
    }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val canSave = productName.isNotBlank() && location.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Food Item" else "Add Food Item") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (!productResolved) return@Scaffold

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Product section ---
            Text("Product", style = MaterialTheme.typography.titleMedium)

            if (existingProduct != null) {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(existingProduct!!.name, style = MaterialTheme.typography.bodyLarge)
                        if (!existingProduct!!.category.isNullOrBlank())
                            Text(existingProduct!!.category!!, style = MaterialTheme.typography.bodySmall)
                        Text(existingProduct!!.trackingType.name, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Barcode (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Product name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (optional)") },
                    placeholder = { Text("e.g. Pantry staple, Canned good") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Tracking type", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrackingType.values().forEach { type ->
                        FilterChip(
                            selected = trackingType == type,
                            onClick = { trackingType = type },
                            label = { Text(if (type == TrackingType.DISCRETE) "Discrete (count)" else "Weight/Volume") }
                        )
                    }
                }
            }

            HorizontalDivider()

            // --- Instance section ---
            Text("Instance details", style = MaterialTheme.typography.titleMedium)

            if (trackingType == TrackingType.WEIGHT_VOLUME) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startingAmount,
                        onValueChange = { startingAmount = it },
                        label = { Text("Starting amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        placeholder = { Text("g, ml, kg…") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (isEdit) {
                    OutlinedTextField(
                        value = remainingAmount,
                        onValueChange = { remainingAmount = it },
                        label = { Text("Remaining amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = { amountUsed = ""; showUseDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Record usage…") }
                }
            } else {
                Text("Tracking: Discrete item", style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = usedStatus == UsedStatus.USED,
                        onCheckedChange = { usedStatus = if (it) UsedStatus.USED else UsedStatus.UNUSED }
                    )
                    Text("Mark as used")
                }
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location *") },
                placeholder = { Text("Pantry, Fridge, Freezer, Garage…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = expiryDateText,
                onValueChange = { expiryDateText = it },
                label = { Text("Expiry date (yyyy-MM-dd, optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val expiry = runCatching { dateFormat.parse(expiryDateText)?.time }.getOrNull()
                    val sa = startingAmount.toDoubleOrNull()
                    val ra = if (isEdit) remainingAmount.toDoubleOrNull() else sa

                    val newProduct = Product(
                        id = existingProduct?.id ?: 0L,
                        barcode = barcode.takeIf { it.isNotBlank() },
                        name = productName.trim(),
                        category = category.takeIf { it.isNotBlank() },
                        trackingType = trackingType
                    )
                    val instance = FoodInstance(
                        id = existingInstance?.id ?: 0L,
                        productId = productId ?: 0L,
                        startingAmount = sa,
                        remainingAmount = ra,
                        unit = unit.takeIf { it.isNotBlank() },
                        usedStatus = if (trackingType == TrackingType.DISCRETE) usedStatus else null,
                        location = location.trim(),
                        expiryDate = expiry,
                        dateAdded = existingInstance?.dateAdded ?: System.currentTimeMillis()
                    )
                    vm.save(
                        existingProductId = productId,
                        newProduct = newProduct,
                        instance = instance,
                        onDone = onDone
                    )
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEdit) "Save Changes" else "Add Item")
            }
        }
    }

    if (showUseDialog) {
        AlertDialog(
            onDismissRequest = { showUseDialog = false },
            title = { Text("Record usage") },
            text = {
                OutlinedTextField(
                    value = amountUsed,
                    onValueChange = { amountUsed = it },
                    label = { Text("Amount used (${unit.ifBlank { "units" }})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val used = amountUsed.toDoubleOrNull() ?: 0.0
                    val current = remainingAmount.toDoubleOrNull() ?: startingAmount.toDoubleOrNull() ?: 0.0
                    remainingAmount = (current - used).coerceAtLeast(0.0).toString()
                    showUseDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showUseDialog = false }) { Text("Cancel") }
            }
        )
    }
}
