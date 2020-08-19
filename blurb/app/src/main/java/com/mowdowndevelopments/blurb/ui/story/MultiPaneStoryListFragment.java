package com.mowdowndevelopments.blurb.ui.story;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mowdowndevelopments.blurb.databinding.FragmentMultiPaneStoryListBinding;
import com.mowdowndevelopments.blurb.ui.feeds.StoryClickListener;


public class MultiPaneStoryListFragment extends Fragment implements StoryClickListener {

    //TODO Multipane stuff
    private StoryViewModel viewModel;
    private FragmentMultiPaneStoryListBinding binding;
    private MultiPaneStoryAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMultiPaneStoryListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new MultiPaneStoryAdapter(this);
        binding.rvStoriesMp.setHasFixedSize(true);
        binding.rvStoriesMp.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvStoriesMp.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(StoryViewModel.class);
        viewModel.getStories().observe(getViewLifecycleOwner(), stories -> {
            if (stories != null){
                adapter.setStories(stories);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStoryClick(int position) {
        viewModel.setIndexToView(position);
    }
}