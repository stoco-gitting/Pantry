package com.pantryplus.data.repository

import com.pantryplus.data.db.dao.ProductDao
import com.pantryplus.data.db.entity.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val dao: ProductDao) {
    val allProducts: Flow<List<Product>> = dao.getAllFlow()

    suspend fun getById(id: Long): Product? = dao.getById(id)
    suspend fun getByBarcode(barcode: String): Product? = dao.getByBarcode(barcode)
    suspend fun insert(product: Product): Long = dao.insert(product)
    suspend fun update(product: Product) = dao.update(product)
    suspend fun delete(product: Product) = dao.delete(product)
    suspend fun getAll(): List<Product> = dao.getAll()
}
