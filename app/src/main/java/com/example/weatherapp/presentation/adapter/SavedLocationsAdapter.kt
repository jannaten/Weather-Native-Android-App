package com.example.weatherapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemSavedLocationBinding
import com.example.weatherapp.domain.model.SavedLocation

class SavedLocationsAdapter(
    private val onLocationClick: (SavedLocation) -> Unit,
    private val onDeleteClick: (SavedLocation) -> Unit
) : ListAdapter<SavedLocation, SavedLocationsAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemSavedLocationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SavedLocation) {
            binding.tvLocationName.text = item.name
            binding.tvCountry.text = item.country
            binding.tvCoords.text = "%.2f°, %.2f°".format(item.latitude, item.longitude)
            binding.root.setOnClickListener { onLocationClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
            if (item.isCurrentLocation) {
                binding.chipCurrentLocation.show()
            } else {
                binding.chipCurrentLocation.hide()
            }
        }

        private fun android.view.View.show() { visibility = android.view.View.VISIBLE }
        private fun android.view.View.hide() { visibility = android.view.View.GONE }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSavedLocationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    private object DiffCallback : DiffUtil.ItemCallback<SavedLocation>() {
        override fun areItemsTheSame(old: SavedLocation, new: SavedLocation) = old.id == new.id
        override fun areContentsTheSame(old: SavedLocation, new: SavedLocation) = old == new
    }
}
