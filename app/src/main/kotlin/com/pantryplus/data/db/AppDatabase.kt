package com.pantryplus.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pantryplus.data.db.dao.FoodInstanceDao
import com.pantryplus.data.db.dao.ProductDao
import com.pantryplus.data.db.dao.ToolDao
import com.pantryplus.data.db.entity.FoodInstance
import com.pantryplus.data.db.entity.Product
import com.pantryplus.data.db.entity.Tool

@Database(
    entities = [Product::class, FoodInstance::class, Tool::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun foodInstanceDao(): FoodInstanceDao
    abstract fun toolDao(): ToolDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pantry_plus.db"
                ).build().also { INSTANCE = it }
            }
    }
}
