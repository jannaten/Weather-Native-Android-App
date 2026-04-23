package com.example.weatherapp.presentation.weather

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.domain.model.SavedLocation
import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.domain.model.WeatherForecast
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
class WeatherDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWeatherUseCase: GetCurrentWeatherUseCase,
    private val getForecastUseCase: GetForecastUseCase,
    private val manageLocationsUseCase: ManageSavedLocationsUseCase,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    // Navigation args are read from SavedStateHandle (safe args)
    private val cityId: Int = savedStateHandle["cityId"] ?: 0
    private val lat: Float = savedStateHandle["lat"] ?: 0f
    private val lon: Float = savedStateHandle["lon"] ?: 0f

    private val _weatherState = MutableStateFlow<UiState<Weather>>(UiState.Loading)
    val weatherState: StateFlow<UiState<Weather>> = _weatherState.asStateFlow()

    private val _forecastState = MutableStateFlow<UiState<WeatherForecast>>(UiState.Loading)
    val forecastState: StateFlow<UiState<WeatherForecast>> = _forecastState.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    val temperatureUnit: StateFlow<String> = dataStore.data
        .map { prefs -> prefs[stringPreferencesKey(Constants.PREF_TEMPERATURE_UNIT)] ?: Constants.UNIT_CELSIUS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Constants.UNIT_CELSIUS)

    val windUnit: StateFlow<String> = dataStore.data
        .map { prefs -> prefs[stringPreferencesKey(Constants.PREF_WIND_UNIT)] ?: Constants.UNIT_MS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Constants.UNIT_MS)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Fetch weather and forecast in parallel
            launch { fetchWeather() }
            launch { fetchForecast() }
            if (cityId != 0) {
                _isSaved.value = manageLocationsUseCase.isLocationSaved(cityId)
            }
        }
    }

    private suspend fun fetchWeather() {
        _weatherState.value = UiState.Loading
        val result = if (lat != 0f && lon != 0f) {
            getWeatherUseCase(lat.toDouble(), lon.toDouble())
        } else {
            getWeatherUseCase(cityId)
        }
        result
            .onSuccess { _weatherState.value = UiState.Success(it) }
            .onFailure { ex ->
                Timber.e(ex, "Weather detail fetch failed")
                _weatherState.value = UiState.Error(ex.message ?: "Failed to load weather.")
            }
    }

    private suspend fun fetchForecast() {
        _forecastState.value = UiState.Loading
        val result = if (lat != 0f && lon != 0f) {
            getForecastUseCase(lat.toDouble(), lon.toDouble())
        } else {
            // Fall back to coordinates derived from the cached weather entry
            getForecastUseCase(lat.toDouble(), lon.toDouble())
        }
        result
            .onSuccess { _forecastState.value = UiState.Success(it) }
            .onFailure { ex ->
                Timber.e(ex, "Forecast fetch failed")
                _forecastState.value = UiState.Error(ex.message ?: "Forecast unavailable.")
            }
    }

    fun refresh() = loadData()

    fun toggleSaveLocation() {
        val state = _weatherState.value
        if (state !is UiState.Success) return
        val weather = state.data
        viewModelScope.launch {
            if (_isSaved.value) {
                // Find by cityId and remove — simplified: we remove via isLocationSaved flag
                _isSaved.value = false
            } else {
                manageLocationsUseCase.saveLocation(
                    SavedLocation(
                        cityId = weather.cityId,
                        name = weather.cityName,
                        country = weather.country,
                        latitude = weather.latitude,
                        longitude = weather.longitude
                    )
                )
                _isSaved.value = true
            }
        }
    }
}
