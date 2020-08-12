package com.mowdowndevelopments.blurb.ui.feedList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.databinding.FragmentFeedListBinding;

import org.jetbrains.annotations.NotNull;

public class FeedListFragment extends Fragment implements FeedListAdapter.ItemOnClickListener {

    FragmentFeedListBinding binding;
    FeedListAdapter adapter;
    FeedListViewModel viewModel;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFeedListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FeedListViewModel.class);
        viewModel.getFeedsResponseData().observe(getViewLifecycleOwner(), getFeedsResponse -> {
            if (getFeedsResponse != null){
                adapter.setData(getFeedsResponse);
            }
        });
        viewModel.getLoadingStatus().observe(getViewLifecycleOwner(), loadingStatus -> {
            switch (loadingStatus){
                case LOADING:
                    binding.pbLoadingSpinner.setVisibility(View.VISIBLE);
                    binding.rvFeedList.setVisibility(View.INVISIBLE);
                    binding.tvErrorText.setVisibility(View.INVISIBLE);
                    break;
                case DONE:
                    binding.pbLoadingSpinner.setVisibility(View.INVISIBLE);
                    binding.rvFeedList.setVisibility(View.VISIBLE);
                    binding.tvErrorText.setVisibility(View.INVISIBLE);
                    break;
                case ERROR:
                    binding.pbLoadingSpinner.setVisibility(View.INVISIBLE);
                    binding.rvFeedList.setVisibility(View.INVISIBLE);
                    binding.tvErrorText.setVisibility(View.VISIBLE);
                    break;
            }
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()){
                Snackbar.make(view, message, BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new FeedListAdapter(this);
    }

    public void loadData(){
        viewModel.loadFeeds();
    }

    @Override
    public void onFeedItemClick(Feed f) {
        NavHostFragment.findNavController(this).navigate(FeedListFragmentDirections
                .actionFeedListFragmentToSingleFeedStoryFragment(f));
    }

    @Override
    public void onFolderItemClick(Folder f) {
        Feed[] feeds = new Feed[f.getFeeds().size()];
        NavHostFragment.findNavController(this).navigate(FeedListFragmentDirections
                .actionFeedListFragmentToRiverOfNewsFragment(f.getFeeds().toArray(feeds)));
    }
}