package com.mowdowndevelopments.blurb.ui.story;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.ui.feeds.BaseStoryViewHolder;
import com.mowdowndevelopments.blurb.ui.feeds.StoryClickListener;

public class MultiPaneStoryAdapter extends RecyclerView.Adapter<MultiPaneStoryAdapter.MultiPaneStoryViewHolder> {

    private StoryClickListener listener;
    private Story[] stories;

    public MultiPaneStoryAdapter(StoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MultiPaneStoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //TODO Inflate UI for Story List Item
        return null;
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
        public void onClick(View view) {
            if (stories != null){
                listener.onStoryClick(getAdapterPosition());
            }
        }
    }
}
