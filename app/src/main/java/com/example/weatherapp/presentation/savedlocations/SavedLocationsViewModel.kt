package com.example.weatherapp.presentation.savedlocations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.domain.model.SavedLocation
import com.example.weatherapp.domain.usecase.ManageSavedLocationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedLocationsViewModel @Inject constructor(
    private val manageLocationsUseCase: ManageSavedLocationsUseCase
) : ViewModel() {

    val savedLocations: StateFlow<List<SavedLocation>> = manageLocationsUseCase
        .getSavedLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteLocation(locationId: Int) {
        viewModelScope.launch {
            manageLocationsUseCase.deleteLocation(locationId)
        }
    }
}
