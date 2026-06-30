package com.pantryplus.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TrackingType { WEIGHT_VOLUME, DISCRETE }

@Entity(
    tableName = "products",
    indices = [Index(value = ["barcode"])]
)
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val barcode: String? = null,
    val name: String,
    val category: String? = null,
    val trackingType: TrackingType
)
