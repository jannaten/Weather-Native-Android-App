package com.example.weatherapp.data.remote.api

import com.example.weatherapp.data.remote.dto.ForecastResponseDto
import com.example.weatherapp.data.remote.dto.WeatherResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    // Current weather endpoints — API key and units are injected via OkHttp interceptor

    @GET("data/2.5/weather")
    suspend fun getWeatherByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): WeatherResponseDto

    @GET("data/2.5/weather")
    suspend fun getWeatherByCity(
        @Query("q") cityQuery: String    // format: "London,GB"
    ): WeatherResponseDto

    @GET("data/2.5/weather")
    suspend fun getWeatherByCityId(
        @Query("id") cityId: Int
    ): WeatherResponseDto

    @GET("data/2.5/weather")
    suspend fun getWeatherByZip(
        @Query("zip") zipQuery: String   // format: "E1,GB"
    ): WeatherResponseDto

    // 5-day / 3-hour forecast endpoints

    @GET("data/2.5/forecast")
    suspend fun getForecastByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("cnt") count: Int = 40   // max items (5 days × 8 slots/day)
    ): ForecastResponseDto

    @GET("data/2.5/forecast")
    suspend fun getForecastByCity(
        @Query("q") cityQuery: String,
        @Query("cnt") count: Int = 40
    ): ForecastResponseDto
}
