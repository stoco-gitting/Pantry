package com.pantryplus.ui.screens.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pantryplus.PantryApp
import com.pantryplus.data.db.entity.FoodInstance
import com.pantryplus.data.db.entity.Product
import com.pantryplus.data.repository.FoodInstanceRepository
import com.pantryplus.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FoodEntry(val instance: FoodInstance, val product: Product)

class FoodViewModel(
    val productRepo: ProductRepository,
    val instanceRepo: FoodInstanceRepository
) : ViewModel() {

    val query = MutableStateFlow("")
    val locationFilter = MutableStateFlow("")

    private val allProducts = productRepo.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val foodEntries = instanceRepo.allInstances
        .combine(allProducts) { instances, products ->
            val productMap = products.associateBy { it.id }
            instances.mapNotNull { inst ->
                productMap[inst.productId]?.let { FoodEntry(inst, it) }
            }
        }
        .combine(query) { entries, q ->
            if (q.isBlank()) entries
            else entries.filter {
                it.product.name.contains(q, ignoreCase = true) ||
                it.product.category?.contains(q, ignoreCase = true) == true ||
                it.instance.location.contains(q, ignoreCase = true)
            }
        }
        .combine(locationFilter) { entries, loc ->
            if (loc.isBlank()) entries
            else entries.filter { it.instance.location.equals(loc, ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun getByBarcode(barcode: String): Product? = productRepo.getByBarcode(barcode)
    suspend fun getProductById(id: Long): Product? = productRepo.getById(id)
    suspend fun getInstanceById(id: Long): FoodInstance? = instanceRepo.getById(id)

    fun save(
        existingProductId: Long?,
        newProduct: Product,
        instance: FoodInstance,
        onDone: () -> Unit
    ) = viewModelScope.launch {
        val pid = existingProductId ?: productRepo.insert(newProduct)
        val inst = instance.copy(productId = pid)
        if (inst.id == 0L) instanceRepo.insert(inst) else instanceRepo.update(inst)
        onDone()
    }

    fun upsertInstance(instance: FoodInstance) = viewModelScope.launch {
        if (instance.id == 0L) instanceRepo.insert(instance) else instanceRepo.update(instance)
    }

    fun deleteInstance(instance: FoodInstance) = viewModelScope.launch { instanceRepo.delete(instance) }
}

class FoodViewModelFactory(private val app: PantryApp) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FoodViewModel(app.productRepository, app.foodInstanceRepository) as T
    }
}
