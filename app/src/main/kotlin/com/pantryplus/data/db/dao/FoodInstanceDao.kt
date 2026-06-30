package com.pantryplus.data.db.dao

import androidx.room.*
import com.pantryplus.data.db.entity.FoodInstance
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodInstanceDao {
    @Query("SELECT * FROM food_instances ORDER BY dateAdded DESC")
    fun getAllFlow(): Flow<List<FoodInstance>>

    @Query("SELECT * FROM food_instances WHERE id = :id")
    suspend fun getById(id: Long): FoodInstance?

    @Query("SELECT * FROM food_instances WHERE productId = :productId")
    fun getByProductFlow(productId: Long): Flow<List<FoodInstance>>

    @Query("""
        SELECT * FROM food_instances
        WHERE expiryDate IS NOT NULL AND expiryDate <= :thresholdMillis
        ORDER BY expiryDate ASC
    """)
    fun getExpiringSoon(thresholdMillis: Long): Flow<List<FoodInstance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(instance: FoodInstance): Long

    @Update
    suspend fun update(instance: FoodInstance)

    @Delete
    suspend fun delete(instance: FoodInstance)

    @Query("SELECT * FROM food_instances")
    suspend fun getAll(): List<FoodInstance>
}
