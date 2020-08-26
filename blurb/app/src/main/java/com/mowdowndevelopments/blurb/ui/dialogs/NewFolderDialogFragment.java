package com.mowdowndevelopments.blurb.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.databinding.FragmentNewFolderDialogBinding;

import java.util.Arrays;
import java.util.Objects;


public class NewFolderDialogFragment extends DialogFragment {

    public static final String ARG_DIALOG_RESULT = "com.mowdowndevelopments.blurb.RESULT";

    FragmentNewFolderDialogBinding binding;
    private ArrayAdapter<String> adapter = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        binding = FragmentNewFolderDialogBinding.inflate(inflater);
        NewFolderDialogFragmentArgs args = NewFolderDialogFragmentArgs.fromBundle(requireArguments());

        String[] folders = args.getFolderNames();
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
                    String newFolderName = Objects.requireNonNull(binding.etNewFolderName.getText()).toString();
                    if (newFolderName.isEmpty()) return;

                    NavController controller = NavHostFragment.findNavController(this);
                    SavedStateHandle handle = Objects.requireNonNull(controller.getPreviousBackStackEntry())
                            .getSavedStateHandle();

                    String nestedUnder;
                    if (adapter != null){
                        nestedUnder = adapter.getItem(binding.spinFolderList.getSelectedItemPosition());
                    } else {
                        nestedUnder = "";
                    }
                    Pair<String, String> result = new Pair<>(newFolderName, nestedUnder);
                    handle.set(ARG_DIALOG_RESULT, result);
                    dialog.dismiss();
                });
        return builder.create();
    }
}