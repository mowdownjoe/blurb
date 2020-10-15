package com.mowdowndevelopments.blurb.ui.dialogs.newFeed

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.databinding.DialogFragmentNewFeedBinding
import com.mowdowndevelopments.blurb.network.responseModels.AutoCompleteResponse
import com.mowdowndevelopments.blurb.ui.navHost.MainViewModel
import java.util.*

/**
 *
 */
class NewFeedDialogFragment : DialogFragment() {
    enum class ResultKeys {
        FEED, FOLDER
    }

    lateinit var binding: DialogFragmentNewFeedBinding
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private lateinit var autoCompleteAdapter: NewFeedAutoCompleteAdapter
    private val viewModel: MainViewModel by activityViewModels()
    private val args by navArgs<NewFeedDialogFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        autoCompleteAdapter = NewFeedAutoCompleteAdapter(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.autoCompleteDialogData.observe(viewLifecycleOwner, { autoCompleteResponses: List<AutoCompleteResponse>? ->
            if (autoCompleteResponses != null) {
                autoCompleteAdapter.setResponseData(autoCompleteResponses)
                binding.etNewFeedUrl.showDropDown()
            }
        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireActivity())
        binding = DialogFragmentNewFeedBinding.inflate(requireActivity().layoutInflater)
        binding.etNewFeedUrl.setAdapter(autoCompleteAdapter)
        binding.etNewFeedUrl.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val feed = autoCompleteAdapter.getItem(position)
            binding.etNewFeedUrl.setText(feed?.url)
        }
        binding.etNewFeedUrl.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                //unused
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length >= binding.etNewFeedUrl.threshold) {
                    viewModel.loadDataForFeedAutoComplete(charSequence.toString())
                }
            }

            override fun afterTextChanged(editable: Editable) {
                //unused
            }
        })
        val folders = args.folderNames
        if (folders != null) {
            spinnerAdapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, listOf(*folders))
            binding.spinFolderList.adapter = spinnerAdapter
        } else {
            binding.spinFolderList.visibility = View.GONE
            binding.tvSpinnerLabel.visibility = View.GONE
        }
        builder.setView(binding.root)
                .setNegativeButton(R.string.btn_cancel) { dialog: DialogInterface, _: Int ->
                    viewModel.clearAutoCompleteDialogData()
                    dialog.cancel()
                }
                .setPositiveButton(R.string.dialog_btn_add_feed) { dialog: DialogInterface, _: Int ->
                    val feedUrl = binding.etNewFeedUrl.text.toString().trim { it <= ' ' }
                    if (feedUrl.isEmpty()) {
                        dialog.dismiss()
                        return@setPositiveButton
                    }
                    val controller = findNavController()
                    val handle = requireNotNull(controller.previousBackStackEntry)
                            .savedStateHandle
                    val folderToUse: String = if (::spinnerAdapter.isInitialized){
                        spinnerAdapter.getItem(binding.spinFolderList.selectedItemPosition)
                                ?.trim { it <= ' ' }.toString()
                    } else {
                        ""
                    }
                    val results = EnumMap<ResultKeys, String>(ResultKeys::class.java)
                    results[ResultKeys.FEED] = feedUrl
                    if (folderToUse.isNotEmpty()) {
                        results[ResultKeys.FOLDER] = folderToUse
                    }
                    handle.set(ARG_RESULT, results)
                    viewModel.clearAutoCompleteDialogData()
                    dialog.dismiss()
                }
        return builder.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            binding.root

    companion object {
        const val ARG_RESULT = "com.mowdowndevelopments.blurb.FEED_RESULT"
    }
}