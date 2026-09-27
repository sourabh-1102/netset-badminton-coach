package com.example.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.platform.AndroidPlatform

actual fun platformDatabaseBuilder(path: String): RoomDatabase.Builder<AppDatabase> =
    Room.databaseBuilder<AppDatabase>(context = AndroidPlatform.appContext, name = path)
