package com.example.weatherapp.domain.model

data class SavedLocation(
    val id: Int = 0,
    val cityId: Int,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val isCurrentLocation: Boolean = false
)
