package com.pantryplus.data.repository

import com.pantryplus.data.db.dao.FoodInstanceDao
import com.pantryplus.data.db.entity.FoodInstance
import kotlinx.coroutines.flow.Flow

class FoodInstanceRepository(private val dao: FoodInstanceDao) {
    val allInstances: Flow<List<FoodInstance>> = dao.getAllFlow()

    fun getExpiringSoon(thresholdMillis: Long): Flow<List<FoodInstance>> =
        dao.getExpiringSoon(thresholdMillis)

    suspend fun getById(id: Long): FoodInstance? = dao.getById(id)
    suspend fun insert(instance: FoodInstance): Long = dao.insert(instance)
    suspend fun update(instance: FoodInstance) = dao.update(instance)
    suspend fun delete(instance: FoodInstance) = dao.delete(instance)
    suspend fun getAll(): List<FoodInstance> = dao.getAll()
}
