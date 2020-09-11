package com.mowdowndevelopments.blurb.ui.feeds.favorite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mowdowndevelopments.blurb.AppExecutors;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.BlurbDb;
import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.ui.feeds.BaseStoryViewHolder;
import com.mowdowndevelopments.blurb.ui.feeds.StoryClickListener;
import com.squareup.picasso.Picasso;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

public class FavoriteStoriesAdapter extends RecyclerView.Adapter<FavoriteStoriesAdapter.FavoriteStoryViewHolder> {

    private StoryClickListener listener;
    private List<Story> storyList;

    public FavoriteStoriesAdapter(StoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteStoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.story_list_item, parent, false);
        return new FavoriteStoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteStoryViewHolder holder, int position) {
        if (storyList != null){
            holder.bind(storyList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (storyList != null){
            return storyList.size();
        }
        return 0;
    }

    public void setStories(List<Story> stories){
        storyList = stories;
        notifyDataSetChanged();
    }

    class FavoriteStoryViewHolder extends BaseStoryViewHolder{

        protected FavoriteStoryViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bind(Story story) {
            binding.tvHeadline.setText(story.getTitle());
            binding.tvStoryAuthors.setText(story.getAuthors());

            Instant instant = Instant.ofEpochSecond(story.getTimestamp());
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            binding.tvStoryTime.setText(dateTime
                    .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));

            if (story.getFeedId() != -1) {
                AppExecutors.getInstance().diskIO().execute(() -> {
                    String feedTitle = BlurbDb.getInstance(itemView.getContext())
                            .blurbDao().getFeedTitle(story.getFeedId());
                    String faviconUrl = BlurbDb.getInstance(itemView.getContext())
                            .blurbDao().getFeedFaviconUrl(story.getFeedId());
                    AppExecutors.getInstance().mainThread().execute(() -> {
                        binding.tvFeedName.setText(feedTitle);
                        Picasso.get().load(faviconUrl)
                                .placeholder(R.drawable.ic_globe)
                                .error(R.drawable.ic_globe)
                                .into(binding.ivStoryFavicon);
                    });
                });
            }
        }

        @Override
        public void onClick(View view) {
            listener.onStoryClick(getAdapterPosition());
        }
    }
}
