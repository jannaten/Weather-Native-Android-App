package com.example.weatherapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemHourlyForecastBinding
import com.example.weatherapp.domain.model.HourlyForecast
import com.example.weatherapp.util.WeatherIconMapper
import com.example.weatherapp.util.formatTemperature
import com.example.weatherapp.util.toFormattedTime

class HourlyForecastAdapter(
    private val useFahrenheit: Boolean = false
) : ListAdapter<HourlyForecast, HourlyForecastAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemHourlyForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HourlyForecast) {
            binding.tvTime.text = item.timestamp.toFormattedTime()
            binding.tvEmoji.text = WeatherIconMapper.getEmoji(item.weatherId)
            binding.tvTemp.text = item.temperature.formatTemperature(useFahrenheit)
            binding.tvPop.text = if (item.precipitationProbability > 0.1) {
                "${(item.precipitationProbability * 100).toInt()}%"
            } else ""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHourlyForecastBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    private object DiffCallback : DiffUtil.ItemCallback<HourlyForecast>() {
        override fun areItemsTheSame(old: HourlyForecast, new: HourlyForecast) =
            old.timestamp == new.timestamp
        override fun areContentsTheSame(old: HourlyForecast, new: HourlyForecast) = old == new
    }
}
