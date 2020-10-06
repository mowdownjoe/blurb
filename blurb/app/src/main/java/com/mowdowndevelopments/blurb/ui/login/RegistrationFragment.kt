package com.mowdowndevelopments.blurb.ui.login

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.databinding.FragmentRegistrationBinding
import com.mowdowndevelopments.blurb.network.LoadingStatus

/**
 */
class RegistrationFragment : Fragment() {
    private lateinit var binding: FragmentRegistrationBinding
    private val viewModel by navGraphViewModels<LoginViewModel>(R.id.login_graph)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loginStatus.observe(viewLifecycleOwner, { loadingStatus: LoadingStatus ->
            when (loadingStatus) {
                LoadingStatus.LOADING -> {
                    binding.loading.visibility = View.VISIBLE
                    binding.etEmail.visibility = View.INVISIBLE
                    binding.btnRegister.visibility = View.INVISIBLE
                    binding.etUsername.visibility = View.INVISIBLE
                    binding.etPassword.visibility = View.INVISIBLE
                }
                LoadingStatus.ERROR, LoadingStatus.DONE, LoadingStatus.WAITING -> {
                    binding.loading.visibility = View.GONE
                    binding.etEmail.visibility = View.VISIBLE
                    binding.btnRegister.visibility = View.VISIBLE
                    binding.etUsername.visibility = View.VISIBLE
                    binding.etPassword.visibility = View.VISIBLE
                }
            }
            if (loadingStatus === LoadingStatus.DONE) {
                val username = requireNotNull(binding.etUsername.text).toString()
                Toast.makeText(requireContext(), getString(R.string.logged_toast,
                        username), Toast.LENGTH_LONG).show()
                completeRegistration()
            }
        })
        viewModel.errorToast.observe(viewLifecycleOwner, { error: String? ->
            if (error != null && error.isNotEmpty()) {
                Snackbar.make(requireView(), error, BaseTransientBottomBar.LENGTH_LONG).show()
            }
        })
        val handle = requireNotNull(findNavController().previousBackStackEntry).savedStateHandle
        handle.set(REGISTRATION_SUCCESS, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.btnRegister.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requireNotNull(requireView().windowInsetsController).hide(WindowInsets.Type.ime())
            }
            beginRegistrationFlow()
        }
    }

    private fun beginRegistrationFlow() {
        val username = requireNotNull(binding.etUsername.text).toString()
        val emailAddress = requireNotNull(binding.etEmail.text).toString()
        val password = requireNotNull(binding.etPassword.text).toString()
        if (username.isEmpty() || emailAddress.isEmpty()) {
            Snackbar.make(requireView(), R.string.missing_registration_error, BaseTransientBottomBar.LENGTH_SHORT).show()
            return
        }
        if (password.isNotEmpty()) {
            viewModel.registerNewAccount(username, password, emailAddress)
        } else {
            viewModel.registerNewAccount(username, emailAddress)
        }
    }

    private fun completeRegistration() {
        val prefs = requireActivity()
                .getSharedPreferences(getString(R.string.shared_pref_file), 0)
        prefs.edit().putBoolean(getString(R.string.logged_in_key), true).apply()
        requireNotNull(findNavController().previousBackStackEntry).savedStateHandle.set(REGISTRATION_SUCCESS, true)
        findNavController().popBackStack()
    }

    companion object {
        const val REGISTRATION_SUCCESS = "com.mowdowndevelopments.blurb.REGISTRATION_CREATED"
    }
}