package com.mowdowndevelopments.blurb.ui.feeds.single;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.network.ResponseModels.FeedContentsResponse;
import com.mowdowndevelopments.blurb.ui.feeds.BaseStoryViewHolder;
import com.mowdowndevelopments.blurb.ui.feeds.StoryClickListener;
import com.squareup.picasso.Picasso;

public class SingleFeedAdapter extends RecyclerView.Adapter<SingleFeedAdapter.SingleFeedStoryViewHolder> {

    //TODO Extend from PagedListAdapter?

    private Feed feed;
    private StoryClickListener listener;
    private Story[] stories;

    public SingleFeedAdapter(Feed feed, StoryClickListener listener) {
        this.feed = feed;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SingleFeedStoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.story_list_item, parent, false);
        return new SingleFeedStoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SingleFeedStoryViewHolder holder, int position) {
        holder.bind(stories[position]);
    }

    @Override
    public int getItemCount() {
        if (stories != null){
            return stories.length;
        }
        return 0;
    }

    public void setData(FeedContentsResponse data){
        stories = data.getStories();
        notifyDataSetChanged();
    }

    class SingleFeedStoryViewHolder extends BaseStoryViewHolder implements View.OnClickListener {

        public SingleFeedStoryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(Story story) {
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
