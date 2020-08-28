package com.mowdowndevelopments.blurb.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mowdowndevelopments.blurb.databinding.FragmentAboutDialogBinding;


public class AboutDialogFragment extends DialogFragment {

    private FragmentAboutDialogBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        binding = FragmentAboutDialogBinding.inflate(requireActivity().getLayoutInflater());

        builder.setView(binding.getRoot());

        return builder.create();
    }
}