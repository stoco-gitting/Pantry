package com.pantryplus.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pantryplus.PantryApp
import com.pantryplus.data.db.AppDatabase
import com.pantryplus.data.db.entity.FoodInstance
import com.pantryplus.data.db.entity.Product
import com.pantryplus.data.db.entity.Tool
import com.pantryplus.data.repository.FoodInstanceRepository
import com.pantryplus.data.repository.ProductRepository
import com.pantryplus.data.repository.ToolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

data class ExportData(
    val version: Int = 1,
    val products: List<Product>,
    val foodInstances: List<FoodInstance>,
    val tools: List<Tool>
)

sealed class SettingsEvent {
    object Idle : SettingsEvent()
    data class ShareFile(val uri: Uri) : SettingsEvent()
    data class Message(val text: String) : SettingsEvent()
}

class SettingsViewModel(
    private val app: PantryApp,
    private val productRepo: ProductRepository,
    private val instanceRepo: FoodInstanceRepository,
    private val toolRepo: ToolRepository
) : ViewModel() {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private val _event = MutableStateFlow<SettingsEvent>(SettingsEvent.Idle)
    val event: StateFlow<SettingsEvent> = _event

    fun export(context: Context) = viewModelScope.launch {
        try {
            val data = ExportData(
                products = productRepo.getAll(),
                foodInstances = instanceRepo.getAll(),
                tools = toolRepo.getAll()
            )
            val json = gson.toJson(data)
            val dir = File(context.getExternalFilesDir(null), "exports").also { it.mkdirs() }
            val file = File(dir, "pantry_export_${System.currentTimeMillis()}.json")
            file.writeText(json)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            _event.value = SettingsEvent.ShareFile(uri)
        } catch (e: Exception) {
            _event.value = SettingsEvent.Message("Export failed: ${e.message}")
        }
    }

    fun import(context: Context, uri: Uri) = viewModelScope.launch {
        try {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                ?: throw IllegalStateException("Cannot read file")
            val data = gson.fromJson(json, ExportData::class.java)

            val db = AppDatabase.getInstance(context)
            // Delete in FK-safe order (instances before products), then insert
            db.foodInstanceDao().getAll().forEach { db.foodInstanceDao().delete(it) }
            db.productDao().getAll().forEach { db.productDao().delete(it) }
            db.toolDao().getAll().forEach { db.toolDao().delete(it) }
            data.products.forEach { db.productDao().insert(it) }
            data.foodInstances.forEach { db.foodInstanceDao().insert(it) }
            data.tools.forEach { db.toolDao().insert(it) }

            _event.value = SettingsEvent.Message("Import successful.")
        } catch (e: Exception) {
            _event.value = SettingsEvent.Message("Import failed: ${e.message}")
        }
    }

    fun clearEvent() { _event.value = SettingsEvent.Idle }
}

class SettingsViewModelFactory(private val app: PantryApp) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SettingsViewModel(app, app.productRepository, app.foodInstanceRepository, app.toolRepository) as T
    }
}
