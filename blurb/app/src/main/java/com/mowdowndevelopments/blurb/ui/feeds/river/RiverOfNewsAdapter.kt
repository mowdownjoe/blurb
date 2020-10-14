package com.mowdowndevelopments.blurb.ui.feeds.river

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import com.mowdowndevelopments.blurb.AppExecutors
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.database.BlurbDb
import com.mowdowndevelopments.blurb.database.entities.Story
import com.mowdowndevelopments.blurb.ui.feeds.BaseStoryViewHolder
import com.mowdowndevelopments.blurb.ui.feeds.StoryClickListener
import com.mowdowndevelopments.blurb.ui.feeds.river.RiverOfNewsAdapter.RiverViewHolder
import com.mowdowndevelopments.blurb.ui.feeds.single.SingleFeedAdapter
import com.squareup.picasso.Picasso

class RiverOfNewsAdapter(private val listener: StoryClickListener) : PagedListAdapter<Story, RiverViewHolder>(SingleFeedAdapter.DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiverViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.story_list_item, parent, false)
        return RiverViewHolder(view)
    }

    override fun onBindViewHolder(holder: RiverViewHolder, position: Int) {
        if (currentList != null) {
            holder.bind(requireNotNull(getItem(position)))
        }
    }

    inner class RiverViewHolder(itemView: View) : BaseStoryViewHolder(itemView) {
        override fun bind(story: Story) {
            super.bind(story)
            if (story.feedId != -1) {
                val executors = AppExecutors.getInstance()
                executors.diskIO().execute {
                    val feedTitle = BlurbDb.getInstance(itemView.context)
                            .blurbDao().getFeedTitle(story.feedId)
                    val faviconUrl = BlurbDb.getInstance(itemView.context)
                            .blurbDao().getFeedFaviconUrl(story.feedId)
                    executors.mainThread().execute { setFeedNameAndIcon(feedTitle, faviconUrl) }
                }
            }
        }

        private fun setFeedNameAndIcon(feedTitle: String, faviconUrl: String) {
            if (feedTitle.isNotEmpty()) {
                binding.tvFeedName.text = feedTitle
            } else {
                binding.tvFeedName.setText(R.string.unknown)
            }
            if (faviconUrl.isNotEmpty()) {
                Picasso.get().load(faviconUrl)
                        .placeholder(R.drawable.ic_globe)
                        .error(R.drawable.ic_globe)
                        .into(binding.ivStoryFavicon)
            } else {
                binding.ivStoryFavicon.setImageResource(R.drawable.ic_globe)
            }
        }

        override fun onClick(view: View) {
            listener.onStoryClick(adapterPosition)
        }
    }
}