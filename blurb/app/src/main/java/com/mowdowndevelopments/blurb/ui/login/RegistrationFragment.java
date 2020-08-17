package com.mowdowndevelopments.blurb.ui.login;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
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

    public static final String REGISTRATION_SUCCESS = "com.mowdowndevelopments.blurb.REGISTRATION_CREATED";

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
                    break;
                case ERROR:
                case DONE:
                case WAITING:
                    binding.loading.setVisibility(View.GONE);
                    binding.etEmail.setVisibility(View.VISIBLE);
                    binding.btnRegister.setVisibility(View.VISIBLE);
                    binding.etUsername.setVisibility(View.VISIBLE);
                    binding.etPassword.setVisibility(View.VISIBLE);
                    break;
            }
            if (loadingStatus == LoadingStatus.DONE){
                String username = Objects.requireNonNull(binding.etUsername.getText()).toString();
                Toast.makeText(requireContext(), getString(R.string.logged_toast,
                        username), Toast.LENGTH_LONG).show();
                completeRegistration();
            }
        });
        viewModel.getErrorToast().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()){
                Snackbar.make(requireView(), error, BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });

        SavedStateHandle handle = Objects.requireNonNull(NavHostFragment.findNavController(this)
                .getPreviousBackStackEntry()).getSavedStateHandle();
        handle.set(REGISTRATION_SUCCESS, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.btnRegister.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Objects.requireNonNull(requireView().getWindowInsetsController()).hide(WindowInsets.Type.ime());
            }
            beginRegistrationFlow();
        });
    }

    private void beginRegistrationFlow() {
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
    }

    private void completeRegistration() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences(getString(R.string.shared_pref_file), 0);
        prefs.edit().putBoolean(getString(R.string.logged_in_key), true).apply();

        NavController controller = NavHostFragment.findNavController(this);
        Objects.requireNonNull(controller.getPreviousBackStackEntry())
                .getSavedStateHandle().set(REGISTRATION_SUCCESS, true);
        controller.popBackStack();

    }

}