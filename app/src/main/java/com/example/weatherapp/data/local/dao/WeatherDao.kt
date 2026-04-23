package com.example.weatherapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherapp.data.local.entity.WeatherEntity

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("SELECT * FROM cached_weather WHERE cityId = :cityId LIMIT 1")
    suspend fun getWeatherByCityId(cityId: Int): WeatherEntity?

    // Evict stale entries older than the given timestamp to keep the cache fresh
    @Query("DELETE FROM cached_weather WHERE cachedAt < :expiryTimestamp")
    suspend fun deleteExpiredWeather(expiryTimestamp: Long)

    @Query("DELETE FROM cached_weather")
    suspend fun clearAll()
}
