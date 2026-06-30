package com.pantryplus.data.repository

import com.pantryplus.data.db.dao.ToolDao
import com.pantryplus.data.db.entity.Tool
import kotlinx.coroutines.flow.Flow

class ToolRepository(private val dao: ToolDao) {
    val allTools: Flow<List<Tool>> = dao.getAllFlow()

    suspend fun getById(id: Long): Tool? = dao.getById(id)
    suspend fun insert(tool: Tool): Long = dao.insert(tool)
    suspend fun update(tool: Tool) = dao.update(tool)
    suspend fun delete(tool: Tool) = dao.delete(tool)
    suspend fun getAll(): List<Tool> = dao.getAll()
}
