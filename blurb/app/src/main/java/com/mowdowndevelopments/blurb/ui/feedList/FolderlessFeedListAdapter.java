package com.mowdowndevelopments.blurb.ui.feedList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.network.responseModels.GetFeedsResponse;

import java.util.LinkedList;

import timber.log.Timber;

public class FolderlessFeedListAdapter extends RecyclerView.Adapter<FolderlessFeedListAdapter.FeedViewHolder> {

    private FeedListAdapter.ItemOnClickListener listener;
    private LinkedList<Feed> feeds;

    public FolderlessFeedListAdapter(FeedListAdapter.ItemOnClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.feed_list_item, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        if (feeds != null){
            holder.bind(feeds.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (feeds != null){
            return feeds.size();
        }
        return 0;
    }

    public void setData(GetFeedsResponse response){
        feeds = new LinkedList<>(response.getFeeds().values());
        notifyDataSetChanged();
        Timber.d("New data received for adapter. Refreshing.");
    }

    class FeedViewHolder extends BaseFeedViewHolder {

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View view) {
            if (feeds != null) {
                listener.onFeedItemClick(feeds.get(getAdapterPosition()));
            }
        }
    }
}
