package com.mowdowndevelopments.blurb.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.databinding.FragmentNewFolderDialogBinding
import java.util.*

class NewFolderDialogFragment : DialogFragment() {
    enum class ResultKeys {
        NEW_FOLDER, NESTED_UNDER
    }

    lateinit var binding: FragmentNewFolderDialogBinding
    private lateinit var adapter: ArrayAdapter<String>
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireActivity())
        binding = FragmentNewFolderDialogBinding.inflate(requireActivity().layoutInflater)

        val folders = NewFolderDialogFragmentArgs.fromBundle(requireArguments()).folderNames

        if (folders != null) {
            adapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, listOf(*folders))
            binding.spinFolderList.adapter = adapter
        } else {
            binding.spinFolderList.visibility = View.GONE
            binding.tvSpinnerLabel.visibility = View.GONE
        }
        builder.setView(binding.root)
                .setNegativeButton(R.string.btn_cancel) { dialog: DialogInterface, _: Int -> dialog.cancel() }
                .setPositiveButton(R.string.dialog_btn_add_folder) { dialog: DialogInterface, _: Int ->
                    val newFolderName = Objects.requireNonNull(binding.etNewFolderName.text).toString()
                    if (newFolderName.trim { it <= ' ' }.isEmpty()) {
                        dialog.dismiss()
                        return@setPositiveButton
                    }
                    val controller = NavHostFragment.findNavController(this)
                    val handle = requireNotNull(controller.previousBackStackEntry)
                            .savedStateHandle
                    val nestedUnder: String? = if (!::adapter.isInitialized) {
                        adapter.getItem(binding.spinFolderList.selectedItemPosition)
                                ?.trim { it <= ' ' }
                    } else {
                        null
                    }
                    val results = EnumMap<ResultKeys, String>(ResultKeys::class.java)
                    results[ResultKeys.NEW_FOLDER] = newFolderName
                    if (nestedUnder != null && nestedUnder.isNotEmpty()) {
                        results[ResultKeys.NESTED_UNDER] = nestedUnder
                    }
                    handle.set(ARG_DIALOG_RESULT, results)
                    dialog.dismiss()
                }
        return builder.create()
    }

    companion object {
        const val ARG_DIALOG_RESULT = "com.mowdowndevelopments.blurb.RESULT"
    }
}