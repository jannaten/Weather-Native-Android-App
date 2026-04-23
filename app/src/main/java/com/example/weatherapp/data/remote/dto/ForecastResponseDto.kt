package com.example.weatherapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ForecastResponseDto(
    @SerializedName("cod") val cod: String,
    @SerializedName("cnt") val cnt: Int,
    @SerializedName("list") val list: List<ForecastItemDto>,
    @SerializedName("city") val city: ForecastCityDto
)

data class ForecastItemDto(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: MainWeatherDto,
    @SerializedName("weather") val weather: List<WeatherConditionDto>,
    @SerializedName("clouds") val clouds: CloudsDto?,
    @SerializedName("wind") val wind: WindDto,
    @SerializedName("visibility") val visibility: Int?,
    @SerializedName("pop") val pop: Double?,            // probability of precipitation
    @SerializedName("dt_txt") val dtTxt: String
)

data class ForecastCityDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("coord") val coord: CoordDto,
    @SerializedName("country") val country: String,
    @SerializedName("timezone") val timezone: Int,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long
)
