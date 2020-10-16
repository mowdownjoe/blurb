package com.mowdowndevelopments.blurb.ui.dialogs.newFeed

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.databinding.AutocompleteItemBinding
import com.mowdowndevelopments.blurb.network.responseModels.AutoCompleteResponse
import java.util.*

class NewFeedAutoCompleteAdapter(context: Context) : ArrayAdapter<AutoCompleteResponse?>(context, R.layout.autocomplete_item, ArrayList()) {
    private lateinit var feeds: List<AutoCompleteResponse>
    override fun getCount(): Int = if (::feeds.isInitialized) {
        feeds.size
    } else 0

    override fun getItem(i: Int): AutoCompleteResponse? = if (::feeds.isInitialized) {
        feeds[i]
    } else null

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var v = view
        if (v == null) {
            val inflater = LayoutInflater.from(context)
            v = inflater.inflate(R.layout.autocomplete_item, parent, false)
        }
        val binding = AutocompleteItemBinding.bind(v!!)
        if (::feeds.isInitialized) {
            val (feedTitle, subscriberCount, tagline) = feeds[position]
            binding.tvFeedLabel.text = feedTitle
            binding.tvFeedSubCount.text = subscriberCount.toString()
            binding.tvTagline.text = tagline
        }
        return binding.root
    }

    fun setResponseData(data: List<AutoCompleteResponse>) {
        feeds = data
        notifyDataSetChanged()
    }
}