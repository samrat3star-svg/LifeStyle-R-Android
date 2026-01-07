package com.lifestyler.android.presentation.breaks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lifestyler.android.data.api.ApiService
import com.lifestyler.android.databinding.FragmentBreaksBinding
import com.lifestyler.android.data.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BreaksFragment : Fragment() {

    private var _binding: FragmentBreaksBinding? = null
    private val binding get() = _binding!!
    
    @Inject
    lateinit var clientRepository: com.lifestyler.android.domain.repository.ClientRepository
    
    private val adapter = BreaksAdapter()
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBreaksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager = PreferenceManager(requireContext())
        
        setupRecyclerView()
        
        val sheetName = preferenceManager.getSheetName()
        if (sheetName != null) {
            fetchBreaks(sheetName)
        } else {
            Toast.makeText(context, "Error: Sheet name missing", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        binding.breaksRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.breaksRecyclerView.adapter = adapter
    }

    private fun fetchBreaks(sheetName: String) {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val result = clientRepository.getBreaks(sheetName)
                if (result.success) {
                    val data = result
                    val items = data.breaks ?: emptyList<com.lifestyler.android.data.model.BreakEntry>()
                    
                    if (items.isEmpty()) {
                        binding.emptyStateText.visibility = View.VISIBLE
                    } else {
                        binding.emptyStateText.visibility = View.GONE
                        adapter.submitList(items)
                        binding.breaksRecyclerView.scrollToPosition(items.size - 1)
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
