package com.lifestyler.android.presentation.auth

import android.os.Bundle
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.lifestyler.android.R
import com.lifestyler.android.databinding.FragmentLoginBinding
import com.lifestyler.android.presentation.auth.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.app.DatePickerDialog
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import com.lifestyler.android.presentation.main.MainActivity
import kotlinx.coroutines.launch
import com.lifestyler.android.data.preference.PreferenceManager

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferenceManager = PreferenceManager(requireContext())
        
        setupViews()
        observeState()
        prefillCredentials()
    }

    private fun prefillCredentials() {
        if (preferenceManager.isRememberMeChecked()) {
            binding.rememberMeCheckbox.isChecked = true
            binding.regCodeEditText.setText(preferenceManager.getRegCode())
            binding.mobileEditText.setText(preferenceManager.getMobile())
        }
    }

    private fun setupViews() {
        binding.loginButton.setOnClickListener {
            handleLoginClick()
        }
        
        binding.registerButton.setOnClickListener {
            navigateToRegister()
        }
        
        binding.forgotPasswordButton.setOnClickListener {
            navigateToForgotPassword()
        }
    }


    private fun handleLoginClick() {
        val regCode = binding.regCodeEditText.text.toString()
        val mobile = binding.mobileEditText.text.toString()
        
        if (validateInput(regCode, mobile)) {
            viewModel.loginUser(regCode, mobile)
        }
    }

    private fun validateInput(regCode: String, mobile: String): Boolean {
        var isValid = true
        
        if (regCode.isEmpty()) {
            binding.regCodeLayout.error = "Registration Code is required"
            isValid = false
        } else {
            binding.regCodeLayout.error = null
        }
        
        if (mobile.isEmpty()) {
            binding.mobileLayout.error = "Mobile Number is required"
            isValid = false
        } else {
            binding.mobileLayout.error = null
        }
        
        return isValid
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: LoginViewModel.LoginState) {
        when (state) {
            is LoginViewModel.LoginState.Loading -> {
                binding.loginButton.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE
            }
            is LoginViewModel.LoginState.Success -> {
                binding.loginButton.isEnabled = true
                binding.progressBar.visibility = View.GONE
                
                // Handle Remember Me
                if (binding.rememberMeCheckbox.isChecked) {
                    preferenceManager.saveCredentials(
                        binding.regCodeEditText.text.toString(),
                        binding.mobileEditText.text.toString()
                    )
                } else {
                    preferenceManager.clearCredentials()
                }
                
                // Save sheetName for background polling
                preferenceManager.saveSheetName(state.sheetName)
                
                val intent = Intent(requireContext(), MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                activity?.finish()
            }
            is LoginViewModel.LoginState.Error -> {
                binding.loginButton.isEnabled = true
                binding.progressBar.visibility = View.GONE
                showError(state.message)
            }
            is LoginViewModel.LoginState.Idle -> {
                binding.loginButton.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun navigateToDashboard(sheetName: String) {
        val bundle = Bundle().apply {
            putString("sheetName", sheetName)
        }
        findNavController().navigate(R.id.action_loginFragment_to_fastingDashboardFragment, bundle)
    }

    private fun navigateToRegister() {
        findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
    }

    private fun navigateToForgotPassword() {
        findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 