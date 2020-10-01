package com.mowdowndevelopments.blurb.ui.feedList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.database.entities.Feed
import com.mowdowndevelopments.blurb.ui.feedList.FolderInnerFeedListAdapter.InnerFeedViewHolder

class FolderInnerFeedListAdapter(private val listener: FeedOnClickListener) : RecyclerView.Adapter<InnerFeedViewHolder>() {
    interface FeedOnClickListener {
        fun onInnerItemClick(feed: Feed)
    }

    private lateinit var feeds: List<Feed>
    fun setInnerFeeds(feeds: List<Feed>) {
        this.feeds = feeds
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerFeedViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.feed_list_item, parent, false)
        return InnerFeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: InnerFeedViewHolder, position: Int) {
        holder.bind(feeds[position])
    }

    override fun getItemCount(): Int = if (!::feeds.isInitialized) {
        feeds.size
    } else 0

    inner class InnerFeedViewHolder(itemView: View) : BaseFeedViewHolder(itemView) {
        override fun onClick(view: View) {
            if (!::feeds.isInitialized) {
                listener.onInnerItemClick(feeds[adapterPosition])
            }
        }
    }
}