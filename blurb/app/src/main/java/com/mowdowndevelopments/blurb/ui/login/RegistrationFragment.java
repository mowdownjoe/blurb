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
import com.mowdowndevelopments.blurb.databinding.FragmentRegistrationBinding;
import com.mowdowndevelopments.blurb.network.LoadingStatus;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegistrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegistrationFragment extends Fragment {

    private FragmentRegistrationBinding binding;
    private LoginViewModel viewModel;

    public RegistrationFragment() {
        // Required empty public constructor
    }


    public static RegistrationFragment newInstance(String param1, String param2) {

        return new RegistrationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(NavHostFragment.findNavController(this)
                .getViewModelStoreOwner(R.id.login_graph)).get(LoginViewModel.class);
        viewModel.getLoginStatus().observe(getViewLifecycleOwner(), loadingStatus -> {
            switch (loadingStatus){
                case LOADING:
                    binding.loading.setVisibility(View.VISIBLE);
                    binding.etEmail.setVisibility(View.INVISIBLE);
                    binding.btnRegister.setVisibility(View.INVISIBLE);
                    binding.etUsername.setVisibility(View.INVISIBLE);
                    binding.etPassword.setVisibility(View.INVISIBLE);
                    return;
                case ERROR:
                case DONE:
                case WAITING:
                    binding.loading.setVisibility(View.GONE);
                    binding.etEmail.setVisibility(View.VISIBLE);
                    binding.btnRegister.setVisibility(View.VISIBLE);
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
                NavHostFragment.findNavController(this).popBackStack(R.id.login_fragment, true);
            }
        });
        viewModel.getErrorToast().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()){
                Snackbar.make(requireView(), error, BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.btnRegister.setOnClickListener(view -> {
            String username = Objects.requireNonNull(binding.etUsername.getText()).toString();
            String emailAddress = Objects.requireNonNull(binding.etEmail.getText()).toString();
            String password = Objects.requireNonNull(binding.etPassword.getText()).toString();
            if (username.isEmpty() || emailAddress.isEmpty()){
                Snackbar.make(requireView(), R.string.missing_registration_error, BaseTransientBottomBar.LENGTH_SHORT).show();
                return;
            }
            if (!password.isEmpty()){
                viewModel.registerNewAccount(username, password, emailAddress);
            } else {
                viewModel.registerNewAccount(username, emailAddress);
            }
        });
    }

}