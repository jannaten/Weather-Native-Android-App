package com.example.weatherapp.presentation.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.domain.usecase.GetCurrentWeatherUseCase
import com.example.weatherapp.domain.usecase.GetForecastUseCase
import com.example.weatherapp.domain.usecase.ManageSavedLocationsUseCase
import com.example.weatherapp.util.Constants
import com.example.weatherapp.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getWeatherUseCase: GetCurrentWeatherUseCase,
    private val getForecastUseCase: GetForecastUseCase,
    private val manageLocationsUseCase: ManageSavedLocationsUseCase,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _weatherState = MutableStateFlow<UiState<Weather>>(UiState.Empty)
    val weatherState: StateFlow<UiState<Weather>> = _weatherState.asStateFlow()

    private val _locationPermissionRequired = MutableStateFlow(false)
    val locationPermissionRequired: StateFlow<Boolean> = _locationPermissionRequired.asStateFlow()

    val temperatureUnit: StateFlow<String> = dataStore.data
        .map { prefs -> prefs[stringPreferencesKey(Constants.PREF_TEMPERATURE_UNIT)] ?: Constants.UNIT_CELSIUS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Constants.UNIT_CELSIUS)

    val windUnit: StateFlow<String> = dataStore.data
        .map { prefs -> prefs[stringPreferencesKey(Constants.PREF_WIND_UNIT)] ?: Constants.UNIT_MS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Constants.UNIT_MS)

    fun fetchWeatherByCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            _weatherState.value = UiState.Loading
            getWeatherUseCase(lat, lon)
                .onSuccess { _weatherState.value = UiState.Success(it) }
                .onFailure { ex ->
                    Timber.e(ex, "Failed to fetch weather by coordinates")
                    _weatherState.value = UiState.Error(
                        ex.message ?: "Unable to fetch weather. Check your connection.",
                        ex
                    )
                }
        }
    }

    fun fetchWeatherByCity(city: String, countryCode: String = "") {
        viewModelScope.launch {
            _weatherState.value = UiState.Loading
            getWeatherUseCase(city, countryCode)
                .onSuccess { _weatherState.value = UiState.Success(it) }
                .onFailure { ex ->
                    Timber.e(ex, "Failed to fetch weather by city")
                    _weatherState.value = UiState.Error(
                        ex.message ?: "City not found. Check the spelling or try adding the country code."
                    )
                }
        }
    }

    fun onLocationPermissionDenied() {
        _locationPermissionRequired.value = true
        if (_weatherState.value is UiState.Loading) {
            _weatherState.value = UiState.Empty
        }
    }

    fun saveCurrentLocation(weather: Weather) {
        viewModelScope.launch {
            val location = com.example.weatherapp.domain.model.SavedLocation(
                cityId = weather.cityId,
                name = weather.cityName,
                country = weather.country,
                latitude = weather.latitude,
                longitude = weather.longitude,
                isCurrentLocation = true
            )
            manageLocationsUseCase.saveLocation(location)
        }
    }
}
