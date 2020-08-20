package com.mowdowndevelopments.blurb.ui.feedList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.databinding.FolderListItemBinding;
import com.mowdowndevelopments.blurb.network.ResponseModels.GetFeedsResponse;

import java.util.ArrayList;
import java.util.Map;

import timber.log.Timber;

@SuppressWarnings("rawtypes")
public class FeedListAdapter extends RecyclerView.Adapter {

    private static final int HOLDER_TYPE_FOLDER = 771;
    private static final int HOLDER_TYPE_ORPHAN_FEED = 367;

    private ArrayList<FeedListItem> feedListItems;
    private ItemOnClickListener listener;

    interface ItemOnClickListener{
        void onFeedItemClick(Feed f);
        void onFolderItemClick(Folder f);
    }

    public FeedListAdapter(ItemOnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        FeedListItem item = feedListItems.get(position);
        if (item instanceof Feed){
            return HOLDER_TYPE_ORPHAN_FEED;
        } else if (item instanceof Folder){
            return HOLDER_TYPE_FOLDER;
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        if (viewType == HOLDER_TYPE_ORPHAN_FEED){
            view = inflater.inflate(R.layout.feed_list_item, parent, false);
            return new OrphanFeedViewHolder(view);
        } else if (viewType == HOLDER_TYPE_FOLDER){
            view = inflater.inflate(R.layout.folder_list_item, parent, false);
            return new FolderViewHolder(view);
        }
        throw new UnsupportedOperationException("Tried to create an unsupported ViewHolder type.");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FolderViewHolder){
            ((FolderViewHolder) holder).bind((Folder) feedListItems.get(position));
        } else if (holder instanceof OrphanFeedViewHolder){
            ((OrphanFeedViewHolder) holder).bind((Feed) feedListItems.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (feedListItems != null) {
            return feedListItems.size();
        }
        return 0;
    }

    public void setData(GetFeedsResponse response){
        feedListItems = new ArrayList<>();
        Map<String, Feed> feedMap = response.getFeeds();
        response.getFolders().forEach((folderName, feedIDs) -> {
            ArrayList<Feed> feeds = new ArrayList<>();
            for (int i : feedIDs) {
                Feed feed = feedMap.get(Integer.toString(i));
                feeds.add(feed);
            }
            if (!folderName.trim().isEmpty()){
                feedListItems.add(new Folder(folderName, feeds));
            } else { //Empty folder name means not sorted into folder
                feedListItems.addAll(0, feeds);
            }
        });
        notifyDataSetChanged();
        Timber.d("New data received for adapter. Refreshing.");
    }

    private class OrphanFeedViewHolder extends BaseFeedViewHolder implements View.OnClickListener {

        public OrphanFeedViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (feedListItems != null){
                listener.onFeedItemClick((Feed) feedListItems.get(getAdapterPosition()));
            }
        }
    }

    private class FolderViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, FolderInnerFeedListAdapter.FeedOnClickListener {

        private FolderListItemBinding binding;
        private boolean areFeedsHidden;
        private FolderInnerFeedListAdapter adapter;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = FolderListItemBinding.bind(itemView);
            binding.folderHeader.ibExpandContractFolder.setOnClickListener(view -> {
                if (areFeedsHidden){
                    binding.rvFolderFeeds.setVisibility(View.VISIBLE);
                    binding.folderHeader.ibExpandContractFolder.setImageResource(R.drawable.ic_baseline_unfold_less);
                    areFeedsHidden = false;
                } else {
                    binding.rvFolderFeeds.setVisibility(View.GONE);
                    binding.folderHeader.ibExpandContractFolder.setImageResource(R.drawable.ic_baseline_unfold_more);
                    areFeedsHidden = true;
                }
            });
            binding.folderHeader.ivFolderIcon.setOnClickListener(this);
            binding.folderHeader.tvFolderName.setOnClickListener(this);

            adapter = new FolderInnerFeedListAdapter(this);
            binding.rvFolderFeeds.setHasFixedSize(true);
            binding.rvFolderFeeds.setAdapter(adapter);
            binding.rvFolderFeeds.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }

        public void bind(Folder folder){
            areFeedsHidden = false;
            binding.rvFolderFeeds.setVisibility(View.VISIBLE);
            binding.folderHeader.tvFolderName.setText(folder.getName());
            adapter.setInnerFeeds(folder.getFeeds());
        }

        @Override
        public void onClick(View view) {
            if (feedListItems != null){
                listener.onFolderItemClick((Folder) feedListItems.get(getAdapterPosition()));
            }
        }

        @Override
        public void onInnerItemClick(Feed feed) {
            listener.onFeedItemClick(feed);
        }
    }

}
