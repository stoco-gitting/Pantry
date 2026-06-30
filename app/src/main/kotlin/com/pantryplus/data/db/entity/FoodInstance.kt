package com.pantryplus.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UsedStatus { UNUSED, USED }

@Entity(
    tableName = "food_instances",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId")]
)
data class FoodInstance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val startingAmount: Double? = null,
    val remainingAmount: Double? = null,
    val unit: String? = null,
    val usedStatus: UsedStatus? = null,
    val location: String,
    val expiryDate: Long? = null,
    val dateAdded: Long = System.currentTimeMillis()
)
