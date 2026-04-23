package com.example.weatherapp.presentation.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    val temperatureUnit: StateFlow<String> = dataStore.data
        .map { prefs -> prefs[stringPreferencesKey(Constants.PREF_TEMPERATURE_UNIT)] ?: Constants.UNIT_CELSIUS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Constants.UNIT_CELSIUS)

    val windUnit: StateFlow<String> = dataStore.data
        .map { prefs -> prefs[stringPreferencesKey(Constants.PREF_WIND_UNIT)] ?: Constants.UNIT_MS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Constants.UNIT_MS)

    fun setTemperatureUnit(unit: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey(Constants.PREF_TEMPERATURE_UNIT)] = unit
            }
        }
    }

    fun setWindUnit(unit: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey(Constants.PREF_WIND_UNIT)] = unit
            }
        }
    }
}
