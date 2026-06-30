package com.pantryplus.ui.screens.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pantryplus.PantryApp
import com.pantryplus.data.db.entity.Tool
import com.pantryplus.data.repository.ToolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ToolsViewModel(private val repo: ToolRepository) : ViewModel() {

    val query = MutableStateFlow("")

    val tools = repo.allTools
        .combine(query) { list, q ->
            if (q.isBlank()) list
            else list.filter { it.name.contains(q, ignoreCase = true) || it.location.contains(q, ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun upsert(tool: Tool) = viewModelScope.launch {
        if (tool.id == 0L) repo.insert(tool) else repo.update(tool)
    }

    fun delete(tool: Tool) = viewModelScope.launch { repo.delete(tool) }

    suspend fun getById(id: Long): Tool? = repo.getById(id)
}

class ToolsViewModelFactory(private val app: PantryApp) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ToolsViewModel(app.toolRepository) as T
    }
}
