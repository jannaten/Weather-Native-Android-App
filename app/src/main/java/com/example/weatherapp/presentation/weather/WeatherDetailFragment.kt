package com.example.weatherapp.presentation.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentWeatherDetailBinding
import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.domain.model.WeatherForecast
import com.example.weatherapp.presentation.adapter.ForecastDayAdapter
import com.example.weatherapp.presentation.adapter.HourlyForecastAdapter
import com.example.weatherapp.util.Constants
import com.example.weatherapp.util.UiState
import com.example.weatherapp.util.WeatherIconMapper
import com.example.weatherapp.util.collectFlow
import com.example.weatherapp.util.formatTemperature
import com.example.weatherapp.util.formatWindSpeed
import com.example.weatherapp.util.hide
import com.example.weatherapp.util.show
import com.example.weatherapp.util.toFormattedTime
import com.example.weatherapp.util.toWindDirection
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class WeatherDetailFragment : Fragment() {

    private var _binding: FragmentWeatherDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeatherDetailViewModel by viewModels()

    private lateinit var hourlyAdapter: HourlyForecastAdapter
    private lateinit var dailyAdapter: ForecastDayAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerViews()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.fabSave.setOnClickListener { viewModel.toggleSaveLocation() }
    }

    private fun setupRecyclerViews() {
        hourlyAdapter = HourlyForecastAdapter()
        dailyAdapter = ForecastDayAdapter()

        binding.rvHourly.apply {
            adapter = hourlyAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }
        binding.rvDaily.apply {
            adapter = dailyAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun observeViewModel() {
        collectFlow(viewModel.weatherState) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                    binding.scrollContent.hide()
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    binding.scrollContent.show()
                    populateWeather(state.data)
                }
                is UiState.Error -> {
                    binding.progressBar.hide()
                    binding.scrollContent.hide()
                }
                is UiState.Empty -> Unit
            }
        }
        collectFlow(viewModel.forecastState) { state ->
            if (state is UiState.Success) {
                populateForecast(state.data)
            }
        }
        collectFlow(viewModel.isSaved) { saved ->
            binding.fabSave.setImageResource(
                if (saved) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
            )
        }
    }

    private fun populateWeather(weather: Weather) {
        val useFahrenheit = viewModel.temperatureUnit.value == Constants.UNIT_FAHRENHEIT
        val windUnit = viewModel.windUnit.value

        with(binding) {
            toolbar.title = "${weather.cityName}, ${weather.country}"
            tvWeatherEmoji.text = WeatherIconMapper.getEmoji(weather.weatherId)
            tvTemperature.text = weather.temperature.formatTemperature(useFahrenheit)
            tvDescription.text = weather.weatherDescription.replaceFirstChar { it.uppercase() }
            tvFeelsLike.text = weather.feelsLike.formatTemperature(useFahrenheit)
            tvHighLow.text = "${weather.tempMax.formatTemperature(useFahrenheit)} / ${weather.tempMin.formatTemperature(useFahrenheit)}"
            tvHumidity.text = "${weather.humidity}%"
            tvWind.text = "${weather.windSpeed.formatWindSpeed(windUnit)} ${weather.windDegree.toWindDirection()}"
            tvPressure.text = "${weather.pressure} hPa"
            tvVisibility.text = "${(weather.visibility / 1000.0).roundToInt()} km"
            tvSunrise.text = weather.sunrise.toFormattedTime(weather.timezone)
            tvSunset.text = weather.sunset.toFormattedTime(weather.timezone)
            tvCloudiness.text = "${weather.cloudiness}%"
        }
    }

    private fun populateForecast(forecast: WeatherForecast) {
        val useFahrenheit = viewModel.temperatureUnit.value == Constants.UNIT_FAHRENHEIT
        hourlyAdapter = HourlyForecastAdapter(useFahrenheit)
        dailyAdapter = ForecastDayAdapter(useFahrenheit)
        binding.rvHourly.adapter = hourlyAdapter
        binding.rvDaily.adapter = dailyAdapter
        hourlyAdapter.submitList(forecast.hourlyForecasts)
        dailyAdapter.submitList(forecast.dailyForecasts)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
