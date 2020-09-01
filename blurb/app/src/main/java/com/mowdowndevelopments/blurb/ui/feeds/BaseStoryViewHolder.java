package com.mowdowndevelopments.blurb.ui.feeds;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.databinding.StoryListItemBinding;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class BaseStoryViewHolder extends RecyclerView.ViewHolder {

    protected StoryListItemBinding binding;

    public BaseStoryViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = StoryListItemBinding.bind(itemView);
    }

    public void bind(Story story){
        binding.tvHeadline.setText(story.getTitle());
        binding.tvStoryAuthors.setText(story.getAuthors());
        Instant instant = Instant.ofEpochMilli(story.getTimestamp());
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        binding.tvStoryTime.setText(dateTime
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));
    }
}
