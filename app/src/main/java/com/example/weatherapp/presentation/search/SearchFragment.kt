package com.example.weatherapp.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentSearchBinding
import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.util.UiState
import com.example.weatherapp.util.WeatherIconMapper
import com.example.weatherapp.util.collectFlow
import com.example.weatherapp.util.formatTemperature
import com.example.weatherapp.util.hide
import com.example.weatherapp.util.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupModeToggle()
        setupSearch()
        observeViewModel()
    }

    private fun setupModeToggle() {
        binding.chipGroupMode.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chipCity        -> switchMode(SearchMode.CITY)
                R.id.chipCoords      -> switchMode(SearchMode.COORDINATES)
                R.id.chipZip         -> switchMode(SearchMode.ZIP)
                R.id.chipCityId      -> switchMode(SearchMode.CITY_ID)
            }
        }
    }

    private fun switchMode(mode: SearchMode) {
        viewModel.setSearchMode(mode)
        binding.layoutCity.hide()
        binding.layoutCoords.hide()
        binding.layoutZip.hide()
        binding.layoutCityId.hide()
        when (mode) {
            SearchMode.CITY        -> binding.layoutCity.show()
            SearchMode.COORDINATES -> binding.layoutCoords.show()
            SearchMode.ZIP         -> binding.layoutZip.show()
            SearchMode.CITY_ID     -> binding.layoutCityId.show()
        }
    }

    private fun setupSearch() {
        binding.btnSearch.setOnClickListener { performSearch() }
        binding.etCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { performSearch(); true } else false
        }
        binding.cardResult.setOnClickListener {
            val state = viewModel.searchState.value
            if (state is UiState.Success) {
                val action = SearchFragmentDirections.actionSearchToWeatherDetail(
                    cityId = state.data.cityId,
                    lat = state.data.latitude.toFloat(),
                    lon = state.data.longitude.toFloat()
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun performSearch() {
        hideKeyboard()
        when (viewModel.searchMode.value) {
            SearchMode.CITY        -> viewModel.searchByCity(
                binding.etCity.text.toString(),
                binding.etCountryCode.text.toString()
            )
            SearchMode.COORDINATES -> viewModel.searchByCoordinates(
                binding.etLatitude.text.toString(),
                binding.etLongitude.text.toString()
            )
            SearchMode.ZIP         -> viewModel.searchByZip(
                binding.etZip.text.toString(),
                binding.etZipCountry.text.toString()
            )
            SearchMode.CITY_ID     -> viewModel.searchByCityId(
                binding.etCityId.text.toString()
            )
        }
    }

    private fun observeViewModel() {
        collectFlow(viewModel.searchState) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                    binding.cardResult.hide()
                    binding.tvError.hide()
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    binding.tvError.hide()
                    showResult(state.data)
                }
                is UiState.Error -> {
                    binding.progressBar.hide()
                    binding.cardResult.hide()
                    binding.tvError.show()
                    binding.tvError.text = state.message
                }
                is UiState.Empty -> {
                    binding.progressBar.hide()
                    binding.cardResult.hide()
                    binding.tvError.hide()
                }
            }
        }
    }

    private fun showResult(weather: Weather) {
        binding.cardResult.show()
        with(binding) {
            tvResultCity.text = "${weather.cityName}, ${weather.country}"
            tvResultEmoji.text = WeatherIconMapper.getEmoji(weather.weatherId)
            tvResultTemp.text = weather.temperature.formatTemperature(false)
            tvResultCondition.text = weather.weatherDescription.replaceFirstChar { it.uppercase() }
            tvResultHumidity.text = "${weather.humidity}%"
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService<InputMethodManager>()
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
