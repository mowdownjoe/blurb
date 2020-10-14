package com.mowdowndevelopments.blurb.ui.feeds.favorite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mowdowndevelopments.blurb.AppExecutors
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.database.BlurbDb
import com.mowdowndevelopments.blurb.database.entities.Story
import com.mowdowndevelopments.blurb.ui.feeds.BaseStoryViewHolder
import com.mowdowndevelopments.blurb.ui.feeds.StoryClickListener
import com.mowdowndevelopments.blurb.ui.feeds.favorite.FavoriteStoriesAdapter.FavoriteStoryViewHolder
import com.squareup.picasso.Picasso
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class FavoriteStoriesAdapter(private val listener: StoryClickListener) : RecyclerView.Adapter<FavoriteStoryViewHolder>() {
    private lateinit var storyList: List<Story>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteStoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.story_list_item, parent, false)
        return FavoriteStoryViewHolder(v)
    }

    override fun onBindViewHolder(holder: FavoriteStoryViewHolder, position: Int) {
        if (::storyList.isInitialized) {
            holder.bind(storyList[position])
        }
    }

    override fun getItemCount(): Int = if (::storyList.isInitialized) {
        storyList.size
    } else 0

    fun setStories(stories: List<Story>) {
        storyList = stories
        notifyDataSetChanged()
    }

    inner class FavoriteStoryViewHolder(itemView: View) : BaseStoryViewHolder(itemView) {
        override fun bind(story: Story) {
            binding.tvHeadline.text = story.title
            binding.tvStoryAuthors.text = story.authors
            val instant = Instant.ofEpochSecond(story.timestamp)
            val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            binding.tvStoryTime.text = dateTime
                    .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
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