package com.example.weatherapp.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.util.Constants
import com.example.weatherapp.util.LocationHelper
import com.example.weatherapp.util.UiState
import com.example.weatherapp.util.WeatherIconMapper
import com.example.weatherapp.util.collectFlow
import com.example.weatherapp.util.formatTemperature
import com.example.weatherapp.util.formatWindSpeed
import com.example.weatherapp.util.hide
import com.example.weatherapp.util.show
import com.example.weatherapp.util.showToast
import com.example.weatherapp.util.toFormattedDate
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    @Inject lateinit var locationHelper: LocationHelper

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchLocationWeather()
        } else {
            viewModel.onLocationPermissionDenied()
            showToast(getString(R.string.location_permission_rationale))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
        checkLocationAndFetch()
    }

    private fun setupClickListeners() {
        binding.fabSearch.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_search)
        }
        binding.btnRetry.setOnClickListener {
            checkLocationAndFetch()
        }
        binding.cardWeatherDetails.setOnClickListener {
            val state = viewModel.weatherState.value
            if (state is UiState.Success) {
                val action = HomeFragmentDirections.actionHomeToWeatherDetail(
                    cityId = state.data.cityId,
                    lat = state.data.latitude.toFloat(),
                    lon = state.data.longitude.toFloat()
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun observeViewModel() {
        collectFlow(viewModel.weatherState) { state ->
            when (state) {
                is UiState.Loading -> showLoading()
                is UiState.Success -> showWeather(state.data)
                is UiState.Error   -> showError(state.message)
                is UiState.Empty   -> showEmpty()
            }
        }
    }

    private fun checkLocationAndFetch() {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            fetchLocationWeather()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun fetchLocationWeather() {
        lifecycleScope.launch {
            locationHelper.getCurrentLocation()
                .onSuccess { location ->
                    Timber.d("Got location: ${location.latitude}, ${location.longitude}")
                    viewModel.fetchWeatherByCoordinates(location.latitude, location.longitude)
                }
                .onFailure { ex ->
                    Timber.e(ex, "Location fetch failed")
                    showToast(getString(R.string.location_unavailable))
                    viewModel.onLocationPermissionDenied()
                }
        }
    }

    private fun showLoading() {
        binding.progressBar.show()
        binding.groupWeather.hide()
        binding.layoutError.hide()
        binding.layoutEmpty.hide()
    }

    private fun showWeather(weather: Weather) {
        binding.progressBar.hide()
        binding.layoutError.hide()
        binding.layoutEmpty.hide()
        binding.groupWeather.show()

        val useFahrenheit = viewModel.temperatureUnit.value == Constants.UNIT_FAHRENHEIT
        val windUnit = viewModel.windUnit.value

        with(binding) {
            tvCityName.text = "${weather.cityName}, ${weather.country}"
            tvWeatherEmoji.text = WeatherIconMapper.getEmoji(weather.weatherId)
            tvTemperature.text = weather.temperature.formatTemperature(useFahrenheit)
            tvWeatherDescription.text = weather.weatherDescription.replaceFirstChar { it.uppercase() }
            tvFeelsLike.text = getString(
                R.string.feels_like_format,
                weather.feelsLike.formatTemperature(useFahrenheit)
            )
            tvHighLow.text = getString(
                R.string.high_low_format,
                weather.tempMax.formatTemperature(useFahrenheit),
                weather.tempMin.formatTemperature(useFahrenheit)
            )
            tvHumidity.text = "${weather.humidity}%"
            tvWindSpeed.text = weather.windSpeed.formatWindSpeed(windUnit)
            tvPressure.text = "${weather.pressure} hPa"
            tvVisibility.text = "${weather.visibility / 1000} km"
            tvLastUpdated.text = getString(
                R.string.last_updated_format,
                weather.timestamp.toFormattedDate(weather.timezone)
            )
        }

        viewModel.saveCurrentLocation(weather)
    }

    private fun showError(message: String) {
        binding.progressBar.hide()
        binding.groupWeather.hide()
        binding.layoutEmpty.hide()
        binding.layoutError.show()
        binding.tvErrorMessage.text = message
    }

    private fun showEmpty() {
        binding.progressBar.hide()
        binding.groupWeather.hide()
        binding.layoutError.hide()
        binding.layoutEmpty.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
