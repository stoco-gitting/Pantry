package com.pantryplus.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tools")
data class Tool(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val location: String,
    val dateAdded: Long = System.currentTimeMillis()
)
