package com.mowdowndevelopments.blurb.ui.feeds.favorite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.databinding.FavoriteStoriesFragmentBinding;
import com.mowdowndevelopments.blurb.ui.feeds.StoryClickListener;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class FavoriteStoriesFragment extends Fragment implements StoryClickListener {

    private FavoriteStoriesAdapter adapter;
    FavoriteStoriesFragmentBinding binding;
    private FavoriteStoriesViewModel viewModel;


    public static FavoriteStoriesFragment newInstance() {
        return new FavoriteStoriesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FavoriteStoriesFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new FavoriteStoriesAdapter(this);
        binding.content.rvStoryList.setAdapter(adapter);
        binding.content.rvStoryList.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FavoriteStoriesViewModel.class);
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()){
                Snackbar.make(binding.content.getRoot(), message, BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
        viewModel.storyList.observe(getViewLifecycleOwner(), stories -> {
            if (stories != null){
                adapter.setStories(stories);
            }
        });
    }

    @Override
    public void onStoryClick(int position) {
        List<Story> storyList = requireNonNull(viewModel.storyList.getValue());
        Story[] stories = new Story[storyList.size()];
        NavHostFragment.findNavController(this).navigate(FavoriteStoriesFragmentDirections
                .actionFavoriteStoriesFragmentToStoryPagerActivity(storyList.toArray(stories))
                .setInitialStory(position));
    }
}