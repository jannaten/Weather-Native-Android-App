package com.example.weatherapp.domain.model

data class Weather(
    val cityId: Int,
    val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,         // Celsius
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val humidity: Int,               // percent
    val pressure: Int,               // hPa
    val windSpeed: Double,           // m/s
    val windDegree: Int,
    val visibility: Int,             // metres
    val cloudiness: Int,             // percent
    val weatherId: Int,              // OWM condition code
    val weatherMain: String,         // e.g. "Rain"
    val weatherDescription: String,  // e.g. "light rain"
    val weatherIcon: String,         // OWM icon code e.g. "10d"
    val sunrise: Long,               // epoch seconds
    val sunset: Long,
    val timezone: Int,               // offset from UTC in seconds
    val timestamp: Long              // when the observation was recorded
)
