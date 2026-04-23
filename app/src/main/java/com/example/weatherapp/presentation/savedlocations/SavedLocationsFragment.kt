package com.example.weatherapp.presentation.savedlocations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentSavedLocationsBinding
import com.example.weatherapp.presentation.adapter.SavedLocationsAdapter
import com.example.weatherapp.util.collectFlow
import com.example.weatherapp.util.hide
import com.example.weatherapp.util.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SavedLocationsFragment : Fragment() {

    private var _binding: FragmentSavedLocationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SavedLocationsViewModel by viewModels()
    private lateinit var locationsAdapter: SavedLocationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedLocationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        locationsAdapter = SavedLocationsAdapter(
            onLocationClick = { location ->
                val action = SavedLocationsFragmentDirections.actionSavedLocationsToWeatherDetail(
                    cityId = location.cityId,
                    lat = location.latitude.toFloat(),
                    lon = location.longitude.toFloat()
                )
                findNavController().navigate(action)
            },
            onDeleteClick = { location ->
                viewModel.deleteLocation(location.id)
            }
        )
        binding.rvLocations.apply {
            adapter = locationsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        // Swipe-to-delete
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val location = locationsAdapter.currentList[vh.adapterPosition]
                viewModel.deleteLocation(location.id)
            }
        }).attachToRecyclerView(binding.rvLocations)
    }

    private fun observeViewModel() {
        collectFlow(viewModel.savedLocations) { locations ->
            locationsAdapter.submitList(locations)
            if (locations.isEmpty()) {
                binding.layoutEmpty.show()
                binding.rvLocations.hide()
            } else {
                binding.layoutEmpty.hide()
                binding.rvLocations.show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
