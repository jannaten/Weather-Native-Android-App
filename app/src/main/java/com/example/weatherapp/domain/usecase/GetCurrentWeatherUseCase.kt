package com.example.weatherapp.domain.usecase

import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.domain.repository.WeatherRepository
import javax.inject.Inject

class GetCurrentWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(lat: Double, lon: Double): Result<Weather> =
        repository.getWeatherByCoordinates(lat, lon)

    suspend operator fun invoke(city: String, countryCode: String = ""): Result<Weather> =
        repository.getWeatherByCity(city, countryCode)

    suspend operator fun invoke(cityId: Int): Result<Weather> =
        repository.getWeatherByCityId(cityId)

    suspend fun byZip(zipCode: String, countryCode: String): Result<Weather> =
        repository.getWeatherByZip(zipCode, countryCode)
}
