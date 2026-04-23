package com.example.weatherapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemForecastDayBinding
import com.example.weatherapp.domain.model.DailyForecast
import com.example.weatherapp.util.WeatherIconMapper
import com.example.weatherapp.util.formatTemperature
import com.example.weatherapp.util.toShortDay

class ForecastDayAdapter(
    private val useFahrenheit: Boolean = false
) : ListAdapter<DailyForecast, ForecastDayAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemForecastDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DailyForecast) {
            binding.tvDay.text = item.date.toShortDay()
            binding.tvEmoji.text = WeatherIconMapper.getEmoji(item.weatherId)
            binding.tvCondition.text = item.weatherMain
            binding.tvHigh.text = item.tempMax.formatTemperature(useFahrenheit)
            binding.tvLow.text = item.tempMin.formatTemperature(useFahrenheit)
            binding.tvPop.text = if (item.precipitationProbability > 0.1) {
                "${(item.precipitationProbability * 100).toInt()}%"
            } else ""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemForecastDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    private object DiffCallback : DiffUtil.ItemCallback<DailyForecast>() {
        override fun areItemsTheSame(old: DailyForecast, new: DailyForecast) =
            old.date == new.date
        override fun areContentsTheSame(old: DailyForecast, new: DailyForecast) = old == new
    }
}
