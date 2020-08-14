package com.mowdowndevelopments.blurb.ui.login;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.databinding.FragmentLoginBinding;
import com.mowdowndevelopments.blurb.network.LoadingStatus;

import java.util.Objects;


public class LoginFragment extends Fragment {

    private LoginViewModel viewModel;
    private FragmentLoginBinding binding;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnLogin.setOnClickListener(v -> {
            String username = Objects.requireNonNull(binding.etUsername.getText()).toString();
            String password = Objects.requireNonNull(binding.etPassword.getText()).toString();
            if (username.isEmpty()){
                Snackbar.make(requireView(), R.string.no_credentials_error, BaseTransientBottomBar.LENGTH_SHORT).show();
                return;
            }
            if (!password.isEmpty()){
                viewModel.login(username, password);
            } else {
                viewModel.login(username);
            }
        });
        binding.btnCreateAccount.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(LoginFragmentDirections.actionCreateAccount()));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(NavHostFragment.findNavController(this)
                .getViewModelStoreOwner(R.id.login_graph)).get(LoginViewModel.class);
        viewModel.getLoginStatus().observe(getViewLifecycleOwner(), loadingStatus -> {
            switch (loadingStatus){
                case LOADING:
                    binding.loading.setVisibility(View.VISIBLE);
                    binding.btnCreateAccount.setVisibility(View.INVISIBLE);
                    binding.btnLogin.setVisibility(View.INVISIBLE);
                    binding.etUsername.setVisibility(View.INVISIBLE);
                    binding.etPassword.setVisibility(View.INVISIBLE);
                    return;
                case ERROR:
                case DONE:
                case WAITING:
                    binding.loading.setVisibility(View.GONE);
                    binding.btnCreateAccount.setVisibility(View.VISIBLE);
                    binding.btnLogin.setVisibility(View.VISIBLE);
                    binding.etUsername.setVisibility(View.VISIBLE);
                    binding.etPassword.setVisibility(View.VISIBLE);
                    return;
            }
            if (loadingStatus == LoadingStatus.DONE){
                SharedPreferences prefs = requireActivity()
                        .getSharedPreferences(getString(R.string.shared_pref_file), 0);
                prefs.edit().putBoolean(getString(R.string.logged_in_key), true).apply();
                Toast.makeText(requireContext(), getString(R.string.logged_toast,
                        binding.etUsername.getText().toString()), Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
        viewModel.getErrorToast().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()){
                Snackbar.make(requireView(), error, BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
    }

}