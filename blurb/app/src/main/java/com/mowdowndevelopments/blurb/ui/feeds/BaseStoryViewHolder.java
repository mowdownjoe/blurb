package com.mowdowndevelopments.blurb.ui.feeds;

import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.databinding.StoryListItemBinding;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public abstract class BaseStoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    protected StoryListItemBinding binding;

    protected BaseStoryViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = StoryListItemBinding.bind(itemView);
        itemView.setOnClickListener(this);
    }

    public void bind(Story story){
        binding.tvHeadline.setText(story.getTitle());
        binding.tvStoryAuthors.setText(story.getAuthors());
        Instant instant = Instant.ofEpochSecond(story.getTimestamp());
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        binding.tvStoryTime.setText(dateTime
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));
        if (story.isRead() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            itemView.setForegroundTintList(AppCompatResources
                    .getColorStateList(itemView.getContext(), android.R.color.darker_gray));
        }
    }
}
