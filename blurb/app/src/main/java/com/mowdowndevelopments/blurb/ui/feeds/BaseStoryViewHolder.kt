package com.mowdowndevelopments.blurb.ui.feeds

import android.os.Build
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.mowdowndevelopments.blurb.database.entities.Story
import com.mowdowndevelopments.blurb.databinding.StoryListItemBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

abstract class BaseStoryViewHolder protected constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    @JvmField
    protected var binding: StoryListItemBinding = StoryListItemBinding.bind(itemView)
    open fun bind(story: Story) {
        binding.tvHeadline.text = story.title
        binding.tvStoryAuthors.text = story.authors
        val instant = Instant.ofEpochSecond(story.timestamp)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        binding.tvStoryTime.text = dateTime
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
        if (story.isRead && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            itemView.foregroundTintList = AppCompatResources
                    .getColorStateList(itemView.context, android.R.color.darker_gray)
        }
    }

    init {
        itemView.setOnClickListener(this)
    }
}