package com.mowdowndevelopments.blurb.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mowdowndevelopments.blurb.LoginFragmentDirections;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.databinding.FragmentLoginBinding;

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
        binding.login.setOnClickListener(v -> beginLoginFlow());
        //noinspection ResultOfMethodCallIgnored
        binding.tvCreateAccount.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(LoginFragmentDirections.actionCreateAccount()));
    }

    private void beginLoginFlow() {
        String username = Objects.requireNonNull(binding.username.getText()).toString();
        String password = Objects.requireNonNull(binding.password.getText()).toString();
        if (!password.isEmpty()){
            viewModel.login(username, password);
        } else {
            viewModel.login(username);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        viewModel.getLoginStatus().observe(getViewLifecycleOwner(), loadingStatus -> {
            switch (loadingStatus){
                case LOADING:
                    binding.loading.setVisibility(View.VISIBLE);
                    binding.tvCreateAccount.setVisibility(View.INVISIBLE);
                    binding.login.setVisibility(View.INVISIBLE);
                    binding.username.setVisibility(View.INVISIBLE);
                    binding.password.setVisibility(View.INVISIBLE);
                    return;
                case ERROR:
                case DONE:
                case WAITING:
                    binding.loading.setVisibility(View.GONE);
                    binding.tvCreateAccount.setVisibility(View.VISIBLE);
                    binding.login.setVisibility(View.VISIBLE);
                    binding.username.setVisibility(View.VISIBLE);
                    binding.password.setVisibility(View.VISIBLE);
                    return;
            }
        });
        viewModel.getErrorToast().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()){
                Snackbar.make(requireView(), error, BaseTransientBottomBar.LENGTH_LONG)
                        .setAction(R.string.retry_action, view -> beginLoginFlow())
                        .setActionTextColor(getResources().getColor(R.color.secondaryLightColor))
                        .show();
            }
        });
    }

}