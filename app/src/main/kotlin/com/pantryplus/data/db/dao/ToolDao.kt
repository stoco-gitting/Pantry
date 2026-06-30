package com.pantryplus.data.db.dao

import androidx.room.*
import com.pantryplus.data.db.entity.Tool
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolDao {
    @Query("SELECT * FROM tools ORDER BY name ASC")
    fun getAllFlow(): Flow<List<Tool>>

    @Query("SELECT * FROM tools WHERE id = :id")
    suspend fun getById(id: Long): Tool?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tool: Tool): Long

    @Update
    suspend fun update(tool: Tool)

    @Delete
    suspend fun delete(tool: Tool)

    @Query("SELECT * FROM tools")
    suspend fun getAll(): List<Tool>
}
