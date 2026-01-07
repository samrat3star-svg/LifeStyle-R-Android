package com.lifestyler.android.presentation.main.patients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lifestyler.android.databinding.FragmentPatientsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PatientsFragment : Fragment() {

    private var _binding: FragmentPatientsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.patientsTitle.text = "Patients"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 