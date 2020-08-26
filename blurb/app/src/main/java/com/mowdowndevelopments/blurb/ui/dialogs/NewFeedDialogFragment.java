package com.mowdowndevelopments.blurb.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.databinding.DialogFragmentNewFeedBinding;
import com.mowdowndevelopments.blurb.network.ResponseModels.AutoCompleteResponse;
import com.mowdowndevelopments.blurb.ui.navHost.MainViewModel;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 */
public class NewFeedDialogFragment extends DialogFragment {

    public static final String ARG_RESULT = "com.mowdowndevelopments.blurb.FEED_RESULT";

    DialogFragmentNewFeedBinding binding;
    private ArrayAdapter<String> spinnerAdapter = null;
    private NewFeedAutoCompleteAdapter autoCompleteAdapter;
    private MainViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        autoCompleteAdapter = new NewFeedAutoCompleteAdapter(requireContext());

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        viewModel.getAutoCompleteDialogData().observe(getViewLifecycleOwner(), autoCompleteResponses -> {
            if (autoCompleteResponses != null){
                autoCompleteAdapter.setResponseData(autoCompleteResponses);
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        binding = DialogFragmentNewFeedBinding.inflate(inflater);
        NewFeedDialogFragmentArgs args = NewFeedDialogFragmentArgs.fromBundle(requireArguments());

        binding.etNewFeedName.setAdapter(autoCompleteAdapter);
        binding.etNewFeedName.setOnItemClickListener((adapterView, view, position, id) -> {
            AutoCompleteResponse feed = autoCompleteAdapter.getItem(position);
            binding.etNewFeedName.setText(feed.getUrl());
        });
        binding.etNewFeedName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //unused
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() >= binding.etNewFeedName.getThreshold()){
                    viewModel.loadDataForFeedAutoComplete(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //unused
            }
        });

        String[] folders = args.getFolderNames();
        if (folders != null) {
            spinnerAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, Arrays.asList(folders));
            binding.spinFolderList.setAdapter(spinnerAdapter);
        } else {
            binding.spinFolderList.setVisibility(View.GONE);
            binding.tvSpinnerLabel.setVisibility(View.GONE);
        }

        builder.setView(binding.getRoot())
                .setNegativeButton(R.string.btn_cancel, (dialog, i) -> dialog.cancel())
                .setPositiveButton(R.string.dialog_btn_add_feed, (dialog, i) -> {
                    String feedUrl = binding.etNewFeedName.getText().toString();
                    if (feedUrl.isEmpty()) return;

                    NavController controller = NavHostFragment.findNavController(this);
                    SavedStateHandle handle = Objects.requireNonNull(controller.getPreviousBackStackEntry())
                            .getSavedStateHandle();

                    String folderToUse;
                    if (spinnerAdapter != null){
                        folderToUse = spinnerAdapter.getItem(binding.spinFolderList.getSelectedItemPosition());
                    } else {
                        folderToUse = "";
                    }
                    Pair<String, String> result = new Pair<>(feedUrl, folderToUse);
                    handle.set(ARG_RESULT, result);
                    dialog.dismiss();
                });
        return super.onCreateDialog(savedInstanceState);
    }
}