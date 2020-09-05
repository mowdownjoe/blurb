package com.mowdowndevelopments.blurb.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.databinding.FragmentNewFolderDialogBinding;

import java.util.Arrays;
import java.util.EnumMap;

import static java.util.Objects.requireNonNull;


public class NewFolderDialogFragment extends DialogFragment {

    public static final String ARG_DIALOG_RESULT = "com.mowdowndevelopments.blurb.RESULT";
    public enum ResultKeys {
        NEW_FOLDER, NESTED_UNDER
    }

    FragmentNewFolderDialogBinding binding;
    private ArrayAdapter<String> adapter = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        binding = FragmentNewFolderDialogBinding.inflate(requireActivity().getLayoutInflater());

        String[] folders = NewFolderDialogFragmentArgs.fromBundle(requireArguments()).getFolderNames();
        if (folders != null) {
            adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, Arrays.asList(folders));
            binding.spinFolderList.setAdapter(adapter);
        } else {
            binding.spinFolderList.setVisibility(View.GONE);
            binding.tvSpinnerLabel.setVisibility(View.GONE);
        }

        builder.setView(binding.getRoot())
                .setNegativeButton(R.string.btn_cancel, (dialog, i) -> dialog.cancel())
                .setPositiveButton(R.string.dialog_btn_add_folder, (dialog, i) -> {
                    String newFolderName = requireNonNull(binding.etNewFolderName.getText()).toString();
                    if (newFolderName.trim().isEmpty()){
                        dialog.dismiss();
                        return;
                    }

                    NavController controller = NavHostFragment.findNavController(this);
                    SavedStateHandle handle = requireNonNull(controller.getPreviousBackStackEntry())
                            .getSavedStateHandle();

                    String nestedUnder;
                    if (adapter != null){
                        nestedUnder = adapter.getItem(binding.spinFolderList.getSelectedItemPosition())
                                .trim();
                    } else {
                        nestedUnder = "";
                    }
                    EnumMap<ResultKeys, String> results = new EnumMap<>(ResultKeys.class);
                    results.put(ResultKeys.NEW_FOLDER, newFolderName);
                    if (!nestedUnder.isEmpty()) {
                        results.put(ResultKeys.NESTED_UNDER, nestedUnder);
                    }
                    handle.set(ARG_DIALOG_RESULT, results);
                    dialog.dismiss();
                });
        return builder.create();
    }
}