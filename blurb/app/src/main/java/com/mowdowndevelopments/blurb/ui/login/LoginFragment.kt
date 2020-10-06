package com.mowdowndevelopments.blurb.ui.login

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.databinding.FragmentLoginBinding
import com.mowdowndevelopments.blurb.network.LoadingStatus

class LoginFragment : Fragment() {
    private val viewModel by navGraphViewModels<LoginViewModel>(R.id.login_graph)
    private lateinit var binding: FragmentLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) { requireActivity().finishAndRemoveTask() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val handle = requireNotNull(findNavController().previousBackStackEntry).savedStateHandle
        handle.set(LOGIN_SUCCESS, false)
        binding.btnLogin.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requireView().windowInsetsController?.hide(WindowInsets.Type.ime())
            } //else case will use WindowInsetsCompat, which is still in alpha
            beginLoginFlow()
        }
        binding.etPassword.setOnEditorActionListener { _: TextView?, _: Int, keyEvent: KeyEvent? ->
            if (keyEvent != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    requireView().windowInsetsController?.hide(WindowInsets.Type.ime())
                } //else case will use WindowInsetsCompat, which is still in alpha
                beginLoginFlow()
                return@setOnEditorActionListener true
            }
            false
        }
        binding.btnCreateAccount.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionCreateAccount())
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.loginStatus.observe(viewLifecycleOwner, { loadingStatus: LoadingStatus ->
            when (loadingStatus) {
                LoadingStatus.LOADING -> {
                    binding.loading.visibility = View.VISIBLE
                    binding.btnCreateAccount.visibility = View.INVISIBLE
                    binding.btnLogin.visibility = View.INVISIBLE
                    binding.etUsername.visibility = View.INVISIBLE
                    binding.etPassword.visibility = View.INVISIBLE
                }
                LoadingStatus.ERROR, LoadingStatus.DONE, LoadingStatus.WAITING -> {
                    binding.loading.visibility = View.GONE
                    binding.btnCreateAccount.visibility = View.VISIBLE
                    binding.btnLogin.visibility = View.VISIBLE
                    binding.etUsername.visibility = View.VISIBLE
                    binding.etPassword.visibility = View.VISIBLE
                }
            }
            if (loadingStatus === LoadingStatus.DONE) {
                val username = requireNotNull(binding.etUsername.text).toString()
                Toast.makeText(requireContext(), getString(R.string.logged_toast,
                        username), Toast.LENGTH_LONG).show()
                completeLogin()
            }
        })
        viewModel.errorToast.observe(viewLifecycleOwner, { error: String? ->
            if (error != null && error.isNotEmpty()) {
                Snackbar.make(requireView(), error, BaseTransientBottomBar.LENGTH_LONG).show()
            }
        })
        val handle = requireNotNull(findNavController().currentBackStackEntry).savedStateHandle
        handle.getLiveData<Boolean>(RegistrationFragment.REGISTRATION_SUCCESS)
                .observe(viewLifecycleOwner, { loggedIn: Boolean -> if (loggedIn) completeLogin() })
    }

    private fun beginLoginFlow() {
        val username = requireNotNull(binding.etUsername.text).toString()
        val password = requireNotNull(binding.etPassword.text).toString()
        if (username.isEmpty()) {
            Snackbar.make(requireView(), R.string.no_credentials_error, BaseTransientBottomBar.LENGTH_SHORT).show()
            return
        }
        if (password.isNotEmpty()) {
            viewModel.login(username, password)
        } else {
            viewModel.login(username)
        }
    }

    private fun completeLogin() {
        val prefs = requireActivity()
                .getSharedPreferences(getString(R.string.shared_pref_file), 0)
        prefs.edit().putBoolean(getString(R.string.logged_in_key), true).apply()
        val handle = requireNotNull(findNavController()
                .previousBackStackEntry).savedStateHandle
        handle.set(LOGIN_SUCCESS, true)
        findNavController().popBackStack()
    }

    companion object {
        const val LOGIN_SUCCESS = "com.mowdowndevelopments.blurb.LOGIN_SUCCESS"
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}