package com.example.weatherapp.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentSettingsBinding
import com.example.weatherapp.util.Constants
import com.example.weatherapp.util.collectFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeSettings()
        setupListeners()
    }

    private fun observeSettings() {
        collectFlow(viewModel.temperatureUnit) { unit ->
            binding.radioGroupTemp.check(
                if (unit == Constants.UNIT_FAHRENHEIT) R.id.radioFahrenheit else R.id.radioCelsius
            )
        }
        collectFlow(viewModel.windUnit) { unit ->
            binding.radioGroupWind.check(
                when (unit) {
                    Constants.UNIT_MPH -> R.id.radioMph
                    Constants.UNIT_KMH -> R.id.radioKmh
                    else               -> R.id.radioMs
                }
            )
        }
    }

    private fun setupListeners() {
        binding.radioGroupTemp.setOnCheckedChangeListener { _, checkedId ->
            val unit = if (checkedId == R.id.radioFahrenheit) Constants.UNIT_FAHRENHEIT else Constants.UNIT_CELSIUS
            viewModel.setTemperatureUnit(unit)
        }
        binding.radioGroupWind.setOnCheckedChangeListener { _, checkedId ->
            val unit = when (checkedId) {
                R.id.radioMph -> Constants.UNIT_MPH
                R.id.radioKmh -> Constants.UNIT_KMH
                else          -> Constants.UNIT_MS
            }
            viewModel.setWindUnit(unit)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
