package com.mowdowndevelopments.blurb.ui.feedList

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.database.entities.Feed
import com.mowdowndevelopments.blurb.databinding.FeedListItemBinding
import com.squareup.picasso.Picasso

abstract class BaseFeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    protected var binding: FeedListItemBinding = FeedListItemBinding.bind(itemView)

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(feed: Feed) {
        val shouldShowEmptyFeeds = itemView.context
                .getSharedPreferences(itemView.context.getString(R.string.shared_pref_file), 0)
                .getBoolean(itemView.context.getString(R.string.pref_key_empty), true)
        if (!shouldShowEmptyFeeds && feed.unreadCount + feed.preferredUnreadCount <= 0) {
            itemView.visibility = View.GONE
            return
        } else {
            itemView.visibility = View.VISIBLE
        }
        binding.tvFeedTitle.text = feed.feedTitle
        if (feed.unreadCount > 0) {
            binding.tvUnreadCount.visibility = View.VISIBLE
            binding.tvUnreadCount.text = feed.unreadCount.toString()
        } else {
            binding.tvUnreadCount.visibility = View.INVISIBLE
        }
        if (feed.preferredUnreadCount > 0) {
            binding.tvPreferredUnreadCount.visibility = View.VISIBLE
            binding.tvPreferredUnreadCount.text = feed.preferredUnreadCount.toString()
        } else {
            binding.tvPreferredUnreadCount.visibility = View.INVISIBLE
        }
        Picasso.get().load(feed.favIconUrl)
                .error(R.drawable.ic_globe)
                .placeholder(R.drawable.ic_globe)
                .into(binding.ivFavIcon)
    }
}