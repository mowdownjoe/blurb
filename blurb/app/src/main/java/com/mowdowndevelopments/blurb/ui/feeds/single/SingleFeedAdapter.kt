package com.mowdowndevelopments.blurb.ui.feeds.single

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import com.mowdowndevelopments.blurb.AppExecutors
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.database.entities.Feed
import com.mowdowndevelopments.blurb.database.entities.Story
import com.mowdowndevelopments.blurb.ui.feeds.BaseStoryViewHolder
import com.mowdowndevelopments.blurb.ui.feeds.StoryClickListener
import com.mowdowndevelopments.blurb.ui.feeds.single.SingleFeedAdapter.SingleFeedStoryViewHolder
import com.squareup.picasso.Picasso
import timber.log.Timber

class SingleFeedAdapter : PagedListAdapter<Story, SingleFeedStoryViewHolder> {
    private var feed: Feed
    private var listener: StoryClickListener

    companion object {
        @JvmField
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Story> = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.storyHash == newItem.storyHash
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }
        }
        val asyncDifferConfig: AsyncDifferConfig<Story>
            get() = AsyncDifferConfig.Builder(DIFF_CALLBACK)
                    .setBackgroundThreadExecutor(AppExecutors.getInstance().networkIO())
                    .build()
    }

    constructor(feed: Feed, listener: StoryClickListener) : super(DIFF_CALLBACK) {
        this.feed = feed
        this.listener = listener
    }

    constructor(config: AsyncDifferConfig<Story?>,
                feed: Feed,
                listener: StoryClickListener) : super(config) {
        this.feed = feed
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleFeedStoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.story_list_item, parent, false)
        return SingleFeedStoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SingleFeedStoryViewHolder, position: Int) {
        if (currentList != null) {
            getItem(position)?.let { holder.bind(it) }
        }
    }

    inner class SingleFeedStoryViewHolder(itemView: View) : BaseStoryViewHolder(itemView) {
        override fun bind(story: Story) {
            Timber.v("Binding new viewholder.")
            super.bind(story)
            binding.tvFeedName.text = feed.feedTitle
            Picasso.get().load(feed.favIconUrl)
                    .placeholder(R.drawable.ic_globe)
                    .error(R.drawable.ic_globe)
                    .into(binding.ivStoryFavicon)
        }

        override fun onClick(view: View) {
            listener.onStoryClick(adapterPosition)
        }
    }
}