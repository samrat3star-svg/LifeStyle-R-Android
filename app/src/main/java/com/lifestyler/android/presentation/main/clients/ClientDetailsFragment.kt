package com.lifestyler.android.presentation.main.clients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.lifestyler.android.databinding.FragmentClientDetailsBinding
import com.lifestyler.android.presentation.main.clients.viewmodel.ClientDetailsViewModel
import com.lifestyler.android.presentation.main.clients.viewmodel.ClientDetailsUiState
import com.lifestyler.android.data.model.ClientUpdateRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ClientDetailsFragment : Fragment() {
    private var _binding: FragmentClientDetailsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ClientDetailsViewModel by viewModels()
    private val args: ClientDetailsFragmentArgs by navArgs()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        loadClientDetails()
    }
    
    private fun setupUI() {
        binding.apply {
            buttonSave.setOnClickListener {
                saveClientChanges()
            }
            
            buttonEdit.setOnClickListener {
                toggleEditMode()
            }
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ClientDetailsUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.scrollViewContent.visibility = View.GONE
                    }
                    is ClientDetailsUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.scrollViewContent.visibility = View.VISIBLE
                        displayClientData(state.client)
                    }
                    is ClientDetailsUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.scrollViewContent.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                    is ClientDetailsUiState.Saving -> {
                        binding.buttonSave.isEnabled = false
                        binding.buttonSave.text = "Saving..."
                    }
                    is ClientDetailsUiState.Saved -> {
                        binding.buttonSave.isEnabled = true
                        binding.buttonSave.text = "Save Changes"
                        Toast.makeText(requireContext(), "Changes saved successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun loadClientDetails() {
        viewModel.loadClientDetails(args.client)
    }
    
    private fun displayClientData(client: com.lifestyler.android.domain.entity.Client) {
        binding.apply {
            textViewClientName.text = client.name
            editTextClientName.setText(client.name)
            editTextClientEmail.setText(client.email)
            editTextClientPhone.setText(client.phone)
            editTextClientAge.setText(client.age.toString())
            editTextClientGender.setText(client.gender)
            editTextClientWeight.setText(client.weight.toString())
            editTextClientHeight.setText(client.height.toString())
            editTextClientGoal.setText(client.goal)
            editTextMedicalConditions.setText(client.medicalConditions)
            editTextMedications.setText(client.medications)
            editTextAllergies.setText(client.allergies)
            editTextEmergencyContact.setText(client.emergencyContact)
            editTextEmergencyPhone.setText(client.emergencyPhone)
            editTextNotes.setText(client.notes)
            textViewStatus.text = client.status
            textViewRegistrationDate.text = client.registrationDate
        }
    }
    
    private fun toggleEditMode() {
        val isEditMode = binding.editTextClientName.visibility == View.VISIBLE
        if (isEditMode) {
            // Switch to view mode
            binding.apply {
                editTextClientName.visibility = View.GONE
                editTextClientEmail.visibility = View.GONE
                editTextClientPhone.visibility = View.GONE
                editTextClientAge.visibility = View.GONE
                editTextClientGender.visibility = View.GONE
                editTextClientWeight.visibility = View.GONE
                editTextClientHeight.visibility = View.GONE
                editTextClientGoal.visibility = View.GONE
                editTextMedicalConditions.visibility = View.GONE
                editTextMedications.visibility = View.GONE
                editTextAllergies.visibility = View.GONE
                editTextEmergencyContact.visibility = View.GONE
                editTextEmergencyPhone.visibility = View.GONE
                editTextNotes.visibility = View.GONE
                buttonSave.visibility = View.GONE
                
                textViewClientName.visibility = View.VISIBLE
                textViewClientEmail.visibility = View.VISIBLE
                textViewClientPhone.visibility = View.VISIBLE
                textViewClientAge.visibility = View.VISIBLE
                textViewClientGender.visibility = View.VISIBLE
                textViewClientWeight.visibility = View.VISIBLE
                textViewClientHeight.visibility = View.VISIBLE
                textViewClientGoal.visibility = View.VISIBLE
                textViewMedicalConditions.visibility = View.VISIBLE
                textViewMedications.visibility = View.VISIBLE
                textViewAllergies.visibility = View.VISIBLE
                textViewEmergencyContact.visibility = View.VISIBLE
                textViewEmergencyPhone.visibility = View.VISIBLE
                textViewNotes.visibility = View.VISIBLE
                buttonEdit.text = "Edit"
            }
        } else {
            // Switch to edit mode
            binding.apply {
                textViewClientName.visibility = View.GONE
                textViewClientEmail.visibility = View.GONE
                textViewClientPhone.visibility = View.GONE
                textViewClientAge.visibility = View.GONE
                textViewClientGender.visibility = View.GONE
                textViewClientWeight.visibility = View.GONE
                textViewClientHeight.visibility = View.GONE
                textViewClientGoal.visibility = View.GONE
                textViewMedicalConditions.visibility = View.GONE
                textViewMedications.visibility = View.GONE
                textViewAllergies.visibility = View.GONE
                textViewEmergencyContact.visibility = View.GONE
                textViewEmergencyPhone.visibility = View.GONE
                textViewNotes.visibility = View.GONE
                
                editTextClientName.visibility = View.VISIBLE
                editTextClientEmail.visibility = View.VISIBLE
                editTextClientPhone.visibility = View.VISIBLE
                editTextClientAge.visibility = View.VISIBLE
                editTextClientGender.visibility = View.VISIBLE
                editTextClientWeight.visibility = View.VISIBLE
                editTextClientHeight.visibility = View.VISIBLE
                editTextClientGoal.visibility = View.VISIBLE
                editTextMedicalConditions.visibility = View.VISIBLE
                editTextMedications.visibility = View.VISIBLE
                editTextAllergies.visibility = View.VISIBLE
                editTextEmergencyContact.visibility = View.VISIBLE
                editTextEmergencyPhone.visibility = View.VISIBLE
                editTextNotes.visibility = View.VISIBLE
                buttonSave.visibility = View.VISIBLE
                buttonEdit.text = "Cancel"
            }
        }
    }
    
    private fun saveClientChanges() {
        binding.apply {
            val name = editTextClientName.text.toString()
            val email = editTextClientEmail.text.toString()
            val phone = editTextClientPhone.text.toString()
            // Add other fields as needed
            val request = ClientUpdateRequest(
                id = args.client.id,
                name = name,
                email = email,
                phone = phone
            )
            viewModel.updateClient(args.client.id, request)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 