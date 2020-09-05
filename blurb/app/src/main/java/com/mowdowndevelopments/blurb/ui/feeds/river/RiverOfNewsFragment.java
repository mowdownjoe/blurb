package com.mowdowndevelopments.blurb.ui.feeds.river;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.RiverOfNewsFragmentArgs;
import com.mowdowndevelopments.blurb.RiverOfNewsFragmentDirections;
import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.databinding.RiverOfNewsFragmentBinding;
import com.mowdowndevelopments.blurb.ui.dialogs.SortOrderDialogFragment;
import com.mowdowndevelopments.blurb.ui.feeds.StoryClickListener;

import java.util.EnumMap;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class RiverOfNewsFragment extends Fragment implements StoryClickListener {

    RiverOfNewsFragmentBinding binding;
    private RiverOfNewsViewModel viewModel;
    private RiverOfNewsAdapter adapter;

    public static RiverOfNewsFragment newInstance() {
        return new RiverOfNewsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = RiverOfNewsFragmentBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new RiverOfNewsAdapter(this);
        binding.content.rvStoryList.setAdapter(adapter);
        binding.content.rvStoryList.setLayoutManager(new LinearLayoutManager(requireContext()));

        viewModel = new ViewModelProvider(this, new RiverOfNewsViewModel.Factory(
                requireActivity().getApplication(),
                RiverOfNewsFragmentArgs.fromBundle(requireArguments()).getFeedsInRiver()
        )).get(RiverOfNewsViewModel.class);
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()){
                Snackbar.make(binding.getRoot(), message, BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
        viewModel.storyList.observe(getViewLifecycleOwner(), stories -> adapter.submitList(stories));
        viewModel.getLoadingStatus().observe(getViewLifecycleOwner(), loadingStatus -> {
            switch (loadingStatus){
                case LOADING:
                    binding.content.rvStoryList.setVisibility(View.INVISIBLE);
                    binding.content.tvErrorText.setVisibility(View.INVISIBLE);
                    binding.content.pbLoadingSpinner.setVisibility(View.VISIBLE);
                    break;
                case WAITING:
                case DONE:
                    binding.content.rvStoryList.setVisibility(View.VISIBLE);
                    binding.content.tvErrorText.setVisibility(View.INVISIBLE);
                    binding.content.pbLoadingSpinner.setVisibility(View.INVISIBLE);
                    binding.content.srfRefreshTab.setRefreshing(false);
                    break;
                case ERROR:
                    binding.content.tvErrorText.setVisibility(View.VISIBLE);
                    binding.content.pbLoadingSpinner.setVisibility(View.INVISIBLE);
                    binding.content.rvStoryList.setVisibility(View.VISIBLE);
                    binding.content.srfRefreshTab.setRefreshing(false);
                    break;
            }
        });
        viewModel.getPageLoadingStatus().observe(getViewLifecycleOwner(), loadingStatus -> {
            switch (loadingStatus){
                case LOADING:
                    binding.content.srfRefreshTab.setRefreshing(true);
                    break;
                case ERROR:
                case WAITING:
                case DONE:
                    binding.content.srfRefreshTab.setRefreshing(false);
                    break;
            }
        });


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        NavController controller = NavHostFragment.findNavController(this);
        SavedStateHandle handle = requireNonNull(controller.getCurrentBackStackEntry())
                .getSavedStateHandle();

        binding.content.srfRefreshTab.setProgressBackgroundColorSchemeResource(R.color.secondaryColor);
        binding.content.srfRefreshTab.setOnRefreshListener(() -> {
            if (handle.contains(SortOrderDialogFragment.ARG_RESULT)){
                EnumMap<SortOrderDialogFragment.ResultKeys, String> result
                        = requireNonNull(handle.get(SortOrderDialogFragment.ARG_RESULT));
                viewModel.refreshWithNewParameters(result.get(SortOrderDialogFragment.ResultKeys.SORT),
                        result.get(SortOrderDialogFragment.ResultKeys.FILTER));
            } else {
                viewModel.simpleRefresh();
            }
        });

        handle.<EnumMap<SortOrderDialogFragment.ResultKeys, String>>getLiveData(SortOrderDialogFragment.ARG_RESULT)
                .observe(getViewLifecycleOwner(), result -> {
                    if (result != null) {
                        viewModel.refreshWithNewParameters(result.get(SortOrderDialogFragment.ResultKeys.SORT),
                                result.get(SortOrderDialogFragment.ResultKeys.FILTER));
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.feed_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        NavController controller = NavHostFragment.findNavController(this);
        switch (item.getItemId()){
            case R.id.mi_mark_all_read:
                viewModel.markAllAsRead();
                controller.popBackStack();
                return true;
            case R.id.mi_sort_filter:
                controller.navigate(RiverOfNewsFragmentDirections
                        .actionRiverOfNewsFragmentToSortFilterDialog());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStoryClick(int position) {
        List<Story> storyList = requireNonNull(adapter.getCurrentList());
        Story[] stories = new Story[storyList.size()];
        NavHostFragment.findNavController(this).navigate(RiverOfNewsFragmentDirections
                .actionRiverOfNewsFragmentToStoryPagerActivity(storyList.toArray(stories))
                .setInitialStory(position));
    }
}