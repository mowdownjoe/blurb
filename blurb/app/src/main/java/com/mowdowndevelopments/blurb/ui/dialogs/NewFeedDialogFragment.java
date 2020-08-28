package com.mowdowndevelopments.blurb.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.databinding.DialogFragmentNewFeedBinding;
import com.mowdowndevelopments.blurb.network.ResponseModels.AutoCompleteResponse;
import com.mowdowndevelopments.blurb.ui.navHost.MainViewModel;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Objects;

/**
 *
 */
public class NewFeedDialogFragment extends DialogFragment {

    public static final String ARG_RESULT = "com.mowdowndevelopments.blurb.FEED_RESULT";
    public enum ResultKeys {
        FEED, FOLDER
    }

    DialogFragmentNewFeedBinding binding;
    private ArrayAdapter<String> spinnerAdapter = null;
    private NewFeedAutoCompleteAdapter autoCompleteAdapter;
    private MainViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        autoCompleteAdapter = new NewFeedAutoCompleteAdapter(requireContext());
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.getAutoCompleteDialogData().observe(getViewLifecycleOwner(), autoCompleteResponses -> {
            if (autoCompleteResponses != null){
                autoCompleteAdapter.setResponseData(autoCompleteResponses);
                binding.etNewFeedUrl.showDropDown();
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        binding = DialogFragmentNewFeedBinding.inflate(requireActivity().getLayoutInflater());

        binding.etNewFeedUrl.setAdapter(autoCompleteAdapter);
        binding.etNewFeedUrl.setOnItemClickListener((adapterView, view, position, id) -> {
            AutoCompleteResponse feed = autoCompleteAdapter.getItem(position);
            binding.etNewFeedUrl.setText(feed.getUrl());
        });
        binding.etNewFeedUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //unused
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() >= binding.etNewFeedUrl.getThreshold()){
                    viewModel.loadDataForFeedAutoComplete(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //unused
            }
        });

        String[] folders = NewFeedDialogFragmentArgs.fromBundle(requireArguments()).getFolderNames();
        if (folders != null) {
            spinnerAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, Arrays.asList(folders));
            binding.spinFolderList.setAdapter(spinnerAdapter);
        } else {
            binding.spinFolderList.setVisibility(View.GONE);
            binding.tvSpinnerLabel.setVisibility(View.GONE);
        }

        builder.setView(binding.getRoot())
                .setNegativeButton(R.string.btn_cancel, (dialog, i) -> {
                    viewModel.clearAutoCompleteDialogData();
                    dialog.cancel();
                })
                .setPositiveButton(R.string.dialog_btn_add_feed, (dialog, i) -> {
                    String feedUrl = binding.etNewFeedUrl.getText().toString().trim();
                    if (feedUrl.isEmpty()){
                        dialog.dismiss();
                        return;
                    }

                    NavController controller = NavHostFragment.findNavController(this);
                    SavedStateHandle handle = Objects.requireNonNull(controller.getPreviousBackStackEntry())
                            .getSavedStateHandle();

                    String folderToUse;
                    if (spinnerAdapter != null){
                        folderToUse = spinnerAdapter.getItem(binding.spinFolderList.getSelectedItemPosition())
                                .trim();
                    } else {
                        folderToUse = "";
                    }
                    EnumMap<ResultKeys, String> results = new EnumMap<>(ResultKeys.class);
                    results.put(ResultKeys.FEED, feedUrl);
                    if (!folderToUse.isEmpty()) {
                        results.put(ResultKeys.FOLDER, folderToUse);
                    }
                    handle.set(ARG_RESULT, results);
                    viewModel.clearAutoCompleteDialogData();
                    dialog.dismiss();
                });
        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return binding.getRoot();
    }
}