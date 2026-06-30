package com.pantryplus.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pantryplus.PantryApp
import com.pantryplus.data.db.entity.FoodInstance
import com.pantryplus.data.db.entity.Product
import com.pantryplus.data.repository.FoodInstanceRepository
import com.pantryplus.data.repository.ProductRepository
import com.pantryplus.data.repository.ToolRepository
import com.pantryplus.ui.screens.food.FoodEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    productRepo: ProductRepository,
    instanceRepo: FoodInstanceRepository,
    toolRepo: ToolRepository
) : ViewModel() {

    private val expiryWindowMs = 14L * 24 * 60 * 60 * 1000

    val tools = toolRepo.allTools
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val allProducts = productRepo.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val expiringSoon = instanceRepo.getExpiringSoon(System.currentTimeMillis() + expiryWindowMs)
        .combine(allProducts) { instances, products ->
            val map = products.associateBy { it.id }
            instances.mapNotNull { inst -> map[inst.productId]?.let { FoodEntry(inst, it) } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class DashboardViewModelFactory(private val app: PantryApp) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DashboardViewModel(app.productRepository, app.foodInstanceRepository, app.toolRepository) as T
    }
}
