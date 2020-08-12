package com.mowdowndevelopments.blurb.ui.feedList;

import android.annotation.SuppressLint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.databinding.FeedListItemBinding;
import com.squareup.picasso.Picasso;

class BaseFeedViewHolder extends RecyclerView.ViewHolder {

    protected FeedListItemBinding binding;

    public BaseFeedViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = FeedListItemBinding.bind(itemView);
    }

    @SuppressLint("SetTextI18n")
    public void bind(Feed feed){
        binding.tvFeedTitle.setText(feed.getFeedTitle());
        if (feed.getUnreadCount() > 0){
            binding.tvUnreadCount.setVisibility(View.VISIBLE);
            binding.tvUnreadCount.setText(Integer.toString(feed.getUnreadCount()));
        } else {
            binding.tvUnreadCount.setVisibility(View.INVISIBLE);
        }
        if (feed.getPreferredUnreadCount() > 0){
            binding.tvPreferredUnreadCount.setVisibility(View.VISIBLE);
            binding.tvPreferredUnreadCount.setText(Integer.toString(feed.getPreferredUnreadCount()));
        } else {
            binding.tvPreferredUnreadCount.setVisibility(View.INVISIBLE);
        }
        Picasso.get().load(feed.getFavIconUrl())
                .error(R.drawable.ic_baseline_error_24)
                .into(binding.ivFavIcon);

    }
}
