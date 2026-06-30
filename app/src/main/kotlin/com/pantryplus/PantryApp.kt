package com.pantryplus

import android.app.Application
import com.pantryplus.data.db.AppDatabase
import com.pantryplus.data.repository.FoodInstanceRepository
import com.pantryplus.data.repository.ProductRepository
import com.pantryplus.data.repository.ToolRepository

class PantryApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val productRepository by lazy { ProductRepository(database.productDao()) }
    val foodInstanceRepository by lazy { FoodInstanceRepository(database.foodInstanceDao()) }
    val toolRepository by lazy { ToolRepository(database.toolDao()) }
}
