package com.lifestyler.android.presentation.measurements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lifestyler.android.data.api.ApiService
import com.lifestyler.android.data.preference.PreferenceManager
import com.lifestyler.android.databinding.FragmentMeasurementsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MeasurementsFragment : Fragment() {

    private var _binding: FragmentMeasurementsBinding? = null
    private val binding get() = _binding!!
    
    @Inject
    lateinit var apiService: ApiService
    
    private val adapter = MeasurementsAdapter()
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeasurementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager = PreferenceManager(requireContext())
        
        setupRecyclerView()
        
        val sheetName = preferenceManager.getSheetName()
        if (sheetName != null) {
            fetchMeasurements(sheetName)
        } else {
            Toast.makeText(context, "Error: Sheet name missing", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        binding.measurementsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.measurementsRecyclerView.adapter = adapter
    }

    private fun fetchMeasurements(sheetName: String) {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val response = apiService.getMeasurements("getMeasurements", sheetName)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!
                    val items = mutableListOf<MeasurementUiItem>()
                    
                    data.joiningStatus?.let { status ->
                        items.add(
                            MeasurementUiItem(
                                date = "Joining Status",
                                weight = status.weight,
                                height = status.height,
                                chest = status.chest,
                                waist = status.tummy,
                                hips = status.hips,
                                targetWeight = status.targetWeight,
                                isJoiningStatus = true
                            )
                        )
                    }
                    
                    data.history?.forEach { entry ->
                        items.add(
                            MeasurementUiItem(
                                date = entry.date ?: "--",
                                weight = entry.weight,
                                chest = entry.chest,
                                waist = entry.waist,
                                hips = entry.hips,
                                isJoiningStatus = false
                            )
                        )
                    }
                    
                    if (items.isEmpty()) {
                        binding.emptyStateText.visibility = View.VISIBLE
                    } else {
                        binding.emptyStateText.visibility = View.GONE
                        adapter.submitList(items)
                        binding.measurementsRecyclerView.scrollToPosition(items.size - 1)
                    }
                } else {
                    Toast.makeText(context, "Error: ${response.body()?.message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Connection error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar?.visibility = View.GONE
            }
        }
    }
}
