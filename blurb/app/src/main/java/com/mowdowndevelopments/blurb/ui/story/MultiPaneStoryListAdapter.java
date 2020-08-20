package com.mowdowndevelopments.blurb.ui.story;

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

public class MultiPaneStoryListAdapter extends RecyclerView.Adapter<MultiPaneStoryListAdapter.MultiPaneStoryViewHolder> {

    private StoryClickListener listener;
    private Story[] stories;

    public MultiPaneStoryListAdapter(StoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MultiPaneStoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.story_list_item, parent, false);
        return new MultiPaneStoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MultiPaneStoryViewHolder holder, int position) {
        holder.bind(stories[position]);
    }

    @Override
    public int getItemCount() {
        if (stories != null){
            return stories.length;
        }
        return 0;
    }

    public void setStories(Story[] newStories) {
        stories = newStories;
        notifyDataSetChanged();
    }

    class MultiPaneStoryViewHolder extends BaseStoryViewHolder implements View.OnClickListener {

        public MultiPaneStoryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(Story story) {
            super.bind(story);
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
            if (stories != null){
                listener.onStoryClick(getAdapterPosition());
            }
        }
    }
}
