package com.example.data.local

import androidx.room.Room
import androidx.room.RoomDatabase

actual fun platformDatabaseBuilder(path: String): RoomDatabase.Builder<AppDatabase> =
    Room.databaseBuilder<AppDatabase>(name = path)
