package com.example.weatherapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherapp.data.local.entity.SavedLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: SavedLocationEntity)

    @Query("SELECT * FROM saved_locations ORDER BY addedAt DESC")
    fun getAllLocations(): Flow<List<SavedLocationEntity>>

    @Query("SELECT * FROM saved_locations WHERE isCurrentLocation = 1 LIMIT 1")
    suspend fun getCurrentLocation(): SavedLocationEntity?

    @Query("DELETE FROM saved_locations WHERE id = :locationId")
    suspend fun deleteLocation(locationId: Int)

    @Query("DELETE FROM saved_locations WHERE isCurrentLocation = 1")
    suspend fun clearCurrentLocation()

    @Query("SELECT COUNT(*) FROM saved_locations WHERE cityId = :cityId")
    suspend fun isLocationSaved(cityId: Int): Int

    @Query("DELETE FROM saved_locations")
    suspend fun clearAll()
}
