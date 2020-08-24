package com.mowdowndevelopments.blurb.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.mowdowndevelopments.blurb.NewFolderDialogArgs;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.databinding.FragmentNewFolderDialogBinding;

import java.util.Arrays;
import java.util.Objects;


public class NewFolderDialogFragment extends DialogFragment {

    public static final String ARG_DIALOG_RESULT = "com.mowdowndevelopments.blurb.RESULT";

    FragmentNewFolderDialogBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        binding = FragmentNewFolderDialogBinding.inflate(inflater);
        NewFolderDialogArgs args = NewFolderDialogArgs.fromBundle(requireArguments());

        String[] folders = args.getFolderNames();
        ArrayAdapter<String> adapter = null;
        if (folders != null) {
            adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, Arrays.asList(folders));
            binding.spinFolderList.setAdapter(adapter);
        }
        final ArrayAdapter<String> finalAdapter = adapter;

        builder.setView(binding.getRoot())
                .setPositiveButton(R.string.dialog_btn_add_folder, (dialog, i) -> {
                    NavController controller = NavHostFragment.findNavController(this);
                    SavedStateHandle handle = Objects.requireNonNull(controller.getPreviousBackStackEntry())
                            .getSavedStateHandle();

                    String nestedUnder;
                    if (finalAdapter != null){
                        nestedUnder = finalAdapter.getItem(binding.spinFolderList.getSelectedItemPosition());
                    } else {
                        nestedUnder = "";
                    }
                    Pair<String, String> result = new Pair<>(
                            Objects.requireNonNull(binding.etNewFolderName.getText()).toString(),
                            nestedUnder
                    );
                    handle.set(ARG_DIALOG_RESULT, result);
                    dialog.dismiss();
                }).setNegativeButton("Cancel", (dialog, i) -> dialog.cancel());
        return builder.create();
    }
}