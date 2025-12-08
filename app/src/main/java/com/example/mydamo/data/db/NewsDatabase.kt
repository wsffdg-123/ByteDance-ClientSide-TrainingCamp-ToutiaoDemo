package com.example.mydamo.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters // 仅为示例，本例中使用了简单的 String 转换

@Database(
    entities = [NewsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao
}