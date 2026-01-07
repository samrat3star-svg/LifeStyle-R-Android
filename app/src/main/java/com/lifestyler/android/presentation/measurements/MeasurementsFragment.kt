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
    lateinit var clientRepository: com.lifestyler.android.domain.repository.ClientRepository
    
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
        
        lifecycleScope.launch {
            try {
                val result = clientRepository.getMeasurements(sheetName)
                if (result.success) {
                    val data = result
                    val items = mutableListOf<MeasurementUiItem>()
                    
                    data.joiningStatus?.let { status: com.lifestyler.android.data.model.JoiningStatus ->
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
                    
                    data.history?.forEach { entry: com.lifestyler.android.data.model.MeasurementEntry ->
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
                    Toast.makeText(context, "Error: ${result.message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Connection error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar?.visibility = View.GONE
            }
        }
    }
}
