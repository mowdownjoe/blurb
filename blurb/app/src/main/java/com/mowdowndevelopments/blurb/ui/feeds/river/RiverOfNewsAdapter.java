package com.mowdowndevelopments.blurb.ui.feeds.river;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;

import com.mowdowndevelopments.blurb.AppExecutors;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.BlurbDb;
import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.ui.feeds.BaseStoryViewHolder;
import com.mowdowndevelopments.blurb.ui.feeds.StoryClickListener;
import com.mowdowndevelopments.blurb.ui.feeds.single.SingleFeedAdapter;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import static java.util.Objects.requireNonNull;

public class RiverOfNewsAdapter extends PagedListAdapter<Story, RiverOfNewsAdapter.RiverViewHolder> {
    private StoryClickListener listener;

    protected RiverOfNewsAdapter(@NonNull StoryClickListener listener) {
        super(SingleFeedAdapter.DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public RiverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.story_list_item, parent, false);
        return new RiverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RiverViewHolder holder, int position) {
        if (getCurrentList() != null){
            holder.bind(requireNonNull(getItem(position)));
        }
    }

    class RiverViewHolder extends BaseStoryViewHolder {

        public RiverViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bind(@NotNull Story story) {
            binding.tvHeadline.setText(story.getTitle());
            binding.tvStoryAuthors.setText(story.getAuthors());

            Instant instant = Instant.ofEpochSecond(story.getTimestamp());
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            binding.tvStoryTime.setText(dateTime
                    .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));

            AppExecutors.getInstance().diskIO().execute(() -> {
                String feedTitle = BlurbDb.getInstance(itemView.getContext())
                        .blurbDao().getFeedTitle(story.getFeedId());
                String faviconUrl = BlurbDb.getInstance(itemView.getContext())
                        .blurbDao().getFeedFaviconUrl(story.getFeedId());
                binding.tvFeedName.setText(feedTitle);
                Picasso.get().load(faviconUrl)
                        .placeholder(R.drawable.ic_globe)
                        .error(R.drawable.ic_globe)
                        .into(binding.ivStoryFavicon);
            });
        }

        @Override
        public void onClick(View view) {
            listener.onStoryClick(getAdapterPosition());
        }
    }
}
