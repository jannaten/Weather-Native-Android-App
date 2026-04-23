package com.example.weatherapp.domain.repository

import com.example.weatherapp.domain.model.SavedLocation
import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.domain.model.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    // --- Current weather ---

    suspend fun getWeatherByCoordinates(lat: Double, lon: Double): Result<Weather>

    suspend fun getWeatherByCity(city: String, countryCode: String = ""): Result<Weather>

    suspend fun getWeatherByCityId(cityId: Int): Result<Weather>

    suspend fun getWeatherByZip(zipCode: String, countryCode: String): Result<Weather>

    // --- Forecast ---

    suspend fun getForecastByCoordinates(lat: Double, lon: Double): Result<WeatherForecast>

    suspend fun getForecastByCity(city: String, countryCode: String = ""): Result<WeatherForecast>

    // --- Saved locations (Room-backed, reactive) ---

    fun getSavedLocations(): Flow<List<SavedLocation>>

    suspend fun saveLocation(location: SavedLocation)

    suspend fun deleteLocation(locationId: Int)

    suspend fun isLocationSaved(cityId: Int): Boolean

    // --- Offline cache ---

    suspend fun getCachedWeather(cityId: Int): Weather?
}
