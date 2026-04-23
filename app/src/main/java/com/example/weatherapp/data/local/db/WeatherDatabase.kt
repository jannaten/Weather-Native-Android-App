package com.example.weatherapp.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.weatherapp.data.local.dao.SavedLocationDao
import com.example.weatherapp.data.local.dao.WeatherDao
import com.example.weatherapp.data.local.entity.SavedLocationEntity
import com.example.weatherapp.data.local.entity.WeatherEntity

@Database(
    entities = [WeatherEntity::class, SavedLocationEntity::class],
    version = 1,
    exportSchema = true
)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherDao(): WeatherDao
    abstract fun savedLocationDao(): SavedLocationDao

    companion object {
        const val DATABASE_NAME = "weather_database"
    }
}
