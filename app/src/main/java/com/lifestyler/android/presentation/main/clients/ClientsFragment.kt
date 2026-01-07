package com.lifestyler.android.presentation.main.clients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lifestyler.android.databinding.FragmentClientsBinding
import com.lifestyler.android.presentation.main.clients.viewmodel.ClientsViewModel
import com.lifestyler.android.presentation.main.clients.viewmodel.ClientsUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import com.lifestyler.android.presentation.main.clients.ClientsFragmentDirections

@AndroidEntryPoint
class ClientsFragment : Fragment() {
    private var _binding: FragmentClientsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ClientsViewModel by viewModels()
    private lateinit var clientsAdapter: ClientsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        loadClients()
    }
    
    private fun setupRecyclerView() {
        clientsAdapter = ClientsAdapter { client ->
            navigateToClientDetails(client)
        }
        
        binding.recyclerViewClients.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = clientsAdapter
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ClientsUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.recyclerViewClients.visibility = View.GONE
                        binding.textViewError.visibility = View.GONE
                    }
                    is ClientsUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerViewClients.visibility = View.VISIBLE
                        binding.textViewError.visibility = View.GONE
                        clientsAdapter.submitList(state.clients)
                    }
                    is ClientsUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerViewClients.visibility = View.GONE
                        binding.textViewError.visibility = View.VISIBLE
                        binding.textViewError.text = state.message
                    }
                }
            }
        }
    }
    
    private fun loadClients() {
        viewModel.loadClients()
    }
    
    private fun navigateToClientDetails(client: com.lifestyler.android.domain.entity.Client) {
        val action = ClientsFragmentDirections.actionClientsFragmentToClientDetailsFragment(client)
        findNavController().navigate(action)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 