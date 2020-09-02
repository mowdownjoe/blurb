package com.mowdowndevelopments.blurb.ui.feeds.single;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;

import com.mowdowndevelopments.blurb.AppExecutors;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.ui.feeds.BaseStoryViewHolder;
import com.mowdowndevelopments.blurb.ui.feeds.StoryClickListener;
import com.squareup.picasso.Picasso;

import timber.log.Timber;

public class SingleFeedAdapter extends PagedListAdapter<Story, SingleFeedAdapter.SingleFeedStoryViewHolder> {

    private Feed feed;
    private StoryClickListener listener;

    public static final DiffUtil.ItemCallback<Story> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Story>() {
                @Override
                public boolean areItemsTheSame(@NonNull Story oldItem, @NonNull Story newItem) {
                    return oldItem.getStoryHash().equals(newItem.getStoryHash());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Story oldItem, @NonNull Story newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public SingleFeedAdapter(@NonNull Feed feed, @NonNull StoryClickListener listener) {
        super(DIFF_CALLBACK);
        this.feed = feed;
        this.listener = listener;
    }

    public SingleFeedAdapter(@NonNull AsyncDifferConfig<Story> config,
                             @NonNull Feed feed,
                             @NonNull StoryClickListener listener) {
        super(config);
        this.feed = feed;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SingleFeedStoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.story_list_item, parent, false);
        Timber.v("Inflated new viewholder.");
        return new SingleFeedStoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SingleFeedStoryViewHolder holder, int position) {
        if (getCurrentList() != null) {
            holder.bind(getCurrentList().get(position));
        }
    }

    public static AsyncDifferConfig<Story> getAsyncDifferConfig(){
        return new AsyncDifferConfig.Builder<>(DIFF_CALLBACK)
                .setBackgroundThreadExecutor(AppExecutors.getInstance().networkIO())
                .build();
    }

    class SingleFeedStoryViewHolder extends BaseStoryViewHolder implements View.OnClickListener {

        public SingleFeedStoryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(Story story) {
            Timber.v("Binding new viewholder.");
            super.bind(story);
            binding.tvFeedName.setText(feed.getFeedTitle());
            Picasso.get().load(feed.getFavIconUrl())
                    .placeholder(R.drawable.ic_globe)
                    .error(R.drawable.ic_globe)
                    .into(binding.ivStoryFavicon);
        }

        @Override
        public void onClick(View view) {
            listener.onStoryClick(getAdapterPosition());
        }
    }

}
