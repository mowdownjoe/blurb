package com.mowdowndevelopments.blurb.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.databinding.FragmentSortOrderDialogBinding
import java.util.*

/**
 *
 */
class SortOrderDialogFragment : DialogFragment() {
    enum class ResultKeys {
        SORT, FILTER
    }

    lateinit var binding: FragmentSortOrderDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireActivity())
        binding = FragmentSortOrderDialogBinding.inflate(requireActivity().layoutInflater)
        builder.setView(binding.root)
                .setNegativeButton(R.string.btn_cancel) { dialog: DialogInterface, _: Int -> dialog.cancel() }
                .setPositiveButton(R.string.btn_set_filter) { dialog: DialogInterface, _: Int ->
                    val sortKeys = requireContext().resources.getStringArray(R.array.sort_values)
                    val filterKeys = requireContext().resources.getStringArray(R.array.filter_values)
                    val handle = requireNotNull(findNavController().previousBackStackEntry).savedStateHandle
                    val result = EnumMap<ResultKeys, String>(ResultKeys::class.java)
                    result[ResultKeys.SORT] = sortKeys[binding.spinSort.selectedItemPosition]
                    result[ResultKeys.FILTER] = filterKeys[binding.spinSort.selectedItemPosition]
                    handle.set(ARG_RESULT, result)
                    dialog.dismiss()
                }
        return builder.create()
    }

    companion object {
        const val ARG_RESULT = "com.mowdowndevelopments.blurb.FILTER_ARG"
    }
}