package com.example.weatherapp.domain.usecase

import com.example.weatherapp.domain.model.SavedLocation
import com.example.weatherapp.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageSavedLocationsUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    fun getSavedLocations(): Flow<List<SavedLocation>> =
        repository.getSavedLocations()

    suspend fun saveLocation(location: SavedLocation) =
        repository.saveLocation(location)

    suspend fun deleteLocation(locationId: Int) =
        repository.deleteLocation(locationId)

    suspend fun isLocationSaved(cityId: Int): Boolean =
        repository.isLocationSaved(cityId)
}
