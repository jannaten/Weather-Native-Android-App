package com.example.weatherapp.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.domain.usecase.GetCurrentWeatherUseCase
import com.example.weatherapp.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

enum class SearchMode { CITY, COORDINATES, ZIP, CITY_ID }

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getWeatherUseCase: GetCurrentWeatherUseCase
) : ViewModel() {

    private val _searchState = MutableStateFlow<UiState<Weather>>(UiState.Empty)
    val searchState: StateFlow<UiState<Weather>> = _searchState.asStateFlow()

    private val _searchMode = MutableStateFlow(SearchMode.CITY)
    val searchMode: StateFlow<SearchMode> = _searchMode.asStateFlow()

    fun setSearchMode(mode: SearchMode) {
        _searchMode.value = mode
        _searchState.value = UiState.Empty
    }

    fun searchByCity(city: String, countryCode: String = "") {
        if (city.isBlank()) {
            _searchState.value = UiState.Error("Please enter a city name.")
            return
        }
        viewModelScope.launch {
            _searchState.value = UiState.Loading
            getWeatherUseCase(city.trim(), countryCode.trim())
                .onSuccess { _searchState.value = UiState.Success(it) }
                .onFailure { ex ->
                    Timber.e(ex, "City search failed")
                    _searchState.value = UiState.Error(
                        "City \"$city\" not found. Try adding the 2-letter country code (e.g. London,GB)."
                    )
                }
        }
    }

    fun searchByCoordinates(latStr: String, lonStr: String) {
        val lat = latStr.toDoubleOrNull()
        val lon = lonStr.toDoubleOrNull()
        if (lat == null || lon == null || lat !in -90.0..90.0 || lon !in -180.0..180.0) {
            _searchState.value = UiState.Error("Enter valid coordinates (lat: −90…90, lon: −180…180).")
            return
        }
        viewModelScope.launch {
            _searchState.value = UiState.Loading
            getWeatherUseCase(lat, lon)
                .onSuccess { _searchState.value = UiState.Success(it) }
                .onFailure { ex ->
                    Timber.e(ex, "Coordinate search failed")
                    _searchState.value = UiState.Error("No weather data for these coordinates.")
                }
        }
    }

    fun searchByZip(zip: String, countryCode: String) {
        if (zip.isBlank() || countryCode.isBlank()) {
            _searchState.value = UiState.Error("Enter both a postal code and a 2-letter country code.")
            return
        }
        viewModelScope.launch {
            _searchState.value = UiState.Loading
            getWeatherUseCase.byZip(zip.trim(), countryCode.trim())
                .onSuccess { _searchState.value = UiState.Success(it) }
                .onFailure { ex ->
                    Timber.e(ex, "ZIP search failed")
                    _searchState.value = UiState.Error("Postal code \"$zip\" not found for country $countryCode.")
                }
        }
    }

    fun searchByCityId(idStr: String) {
        val id = idStr.toIntOrNull()
        if (id == null) {
            _searchState.value = UiState.Error("City ID must be a number.")
            return
        }
        viewModelScope.launch {
            _searchState.value = UiState.Loading
            getWeatherUseCase(id)
                .onSuccess { _searchState.value = UiState.Success(it) }
                .onFailure { ex ->
                    Timber.e(ex, "City ID search failed")
                    _searchState.value = UiState.Error("No city found with ID $id.")
                }
        }
    }
}
