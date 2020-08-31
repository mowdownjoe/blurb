package com.mowdowndevelopments.blurb.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.databinding.FragmentSortOrderDialogBinding;

import java.util.EnumMap;
import java.util.Objects;

/**
 *
 */
public class SortOrderDialogFragment extends DialogFragment {

    public enum ResultKeys{
        SORT, FILTER
    }

    public static final String ARG_RESULT = "com.mowdowndevelopments.blurb.FILTER_ARG";

    FragmentSortOrderDialogBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        binding = FragmentSortOrderDialogBinding.inflate(requireActivity().getLayoutInflater());

        builder.setView(binding.getRoot())
                .setNegativeButton(R.string.btn_cancel, (dialog, i) -> dialog.cancel())
                .setPositiveButton(R.string.btn_set_filter, (dialog, i) -> {
                    String[] sortKeys = requireContext().getResources().getStringArray(R.array.sort_values);
                    String[] filterKeys = requireContext().getResources().getStringArray(R.array.filter_values);

                    NavController controller = NavHostFragment.findNavController(this);
                    SavedStateHandle handle = Objects.requireNonNull(controller.getPreviousBackStackEntry())
                            .getSavedStateHandle();

                    EnumMap<ResultKeys, String> result = new EnumMap<>(ResultKeys.class);
                    result.put(ResultKeys.SORT, sortKeys[binding.spinSort.getSelectedItemPosition()]);
                    result.put(ResultKeys.FILTER, filterKeys[binding.spinSort.getSelectedItemPosition()]);

                    handle.set(ARG_RESULT, result);
                    dialog.dismiss();
                });

        return builder.create();
    }
}