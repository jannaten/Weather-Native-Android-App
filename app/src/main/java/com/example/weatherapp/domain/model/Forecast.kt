package com.example.weatherapp.domain.model

data class WeatherForecast(
    val cityName: String,
    val country: String,
    val hourlyForecasts: List<HourlyForecast>,
    val dailyForecasts: List<DailyForecast>
)

data class HourlyForecast(
    val timestamp: Long,          // epoch seconds
    val temperature: Double,      // Celsius
    val weatherId: Int,
    val weatherMain: String,
    val weatherIcon: String,
    val precipitationProbability: Double  // 0.0–1.0
)

data class DailyForecast(
    val date: Long,               // epoch seconds (midnight of the day)
    val tempMin: Double,
    val tempMax: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherId: Int,
    val weatherMain: String,
    val weatherDescription: String,
    val weatherIcon: String,
    val precipitationProbability: Double
)
