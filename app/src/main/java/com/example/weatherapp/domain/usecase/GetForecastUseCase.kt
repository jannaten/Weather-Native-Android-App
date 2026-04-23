package com.example.weatherapp.domain.usecase

import com.example.weatherapp.domain.model.WeatherForecast
import com.example.weatherapp.domain.repository.WeatherRepository
import javax.inject.Inject

class GetForecastUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(lat: Double, lon: Double): Result<WeatherForecast> =
        repository.getForecastByCoordinates(lat, lon)

    suspend operator fun invoke(city: String, countryCode: String = ""): Result<WeatherForecast> =
        repository.getForecastByCity(city, countryCode)
}
