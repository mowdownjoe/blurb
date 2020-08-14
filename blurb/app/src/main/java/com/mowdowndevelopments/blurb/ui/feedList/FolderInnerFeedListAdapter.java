package com.mowdowndevelopments.blurb.ui.feedList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.entities.Feed;

import java.util.List;

public class FolderInnerFeedListAdapter extends RecyclerView.Adapter<FolderInnerFeedListAdapter.InnerFeedViewHolder> {

    interface FeedOnClickListener {
        void onInnerItemClick(Feed feed);
    }

    private FeedOnClickListener listener;
    private List<Feed> feeds;

    public FolderInnerFeedListAdapter(FeedOnClickListener listener) {
        this.listener = listener;
    }

    public void setInnerFeeds(List<Feed> feeds){
        this.feeds = feeds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InnerFeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.feed_list_item, parent, false);
        return new InnerFeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerFeedViewHolder holder, int position) {
        holder.bind(feeds.get(position));
    }

    @Override
    public int getItemCount() {
        if (feeds != null) {
            return feeds.size();
        }
        return 0;
    }

    protected class InnerFeedViewHolder extends BaseFeedViewHolder implements View.OnClickListener {

        public InnerFeedViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (feeds != null) {
                listener.onInnerItemClick(feeds.get(getAdapterPosition()));
            }
        }
    }
}
