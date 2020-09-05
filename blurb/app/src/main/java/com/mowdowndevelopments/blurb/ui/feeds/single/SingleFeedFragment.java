package com.mowdowndevelopments.blurb.ui.feeds.single;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.databinding.SingleFeedFragmentBinding;
import com.mowdowndevelopments.blurb.ui.dialogs.SortOrderDialogFragment;
import com.mowdowndevelopments.blurb.ui.feeds.StoryClickListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class SingleFeedFragment extends Fragment implements StoryClickListener {

    SingleFeedFragmentBinding binding;
    private SingleFeedViewModel viewModel;
    private SingleFeedFragmentArgs args;
    private SingleFeedAdapter adapter;

    public static SingleFeedFragment newInstance() {
        return new SingleFeedFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args = SingleFeedFragmentArgs.fromBundle(requireArguments());
        Picasso.get().load(args.getFeedToShow().getFavIconUrl()).fetch(); //Warm up Picasso's cache.
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SingleFeedFragmentBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);

        requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar())
                .setTitle(args.getFeedToShow().getFeedTitle());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new SingleFeedAdapter(args.getFeedToShow(), this);
        binding.content.rvStoryList.setAdapter(adapter);
        binding.content.rvStoryList.setLayoutManager(new LinearLayoutManager(requireContext()));

        viewModel = new ViewModelProvider(this, new SingleFeedViewModel
                .Factory(requireActivity().getApplication(), args.getFeedToShow().getId()))
                .get(SingleFeedViewModel.class);
        setupObservers();
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
                EnumMap<SortOrderDialogFragment.ResultKeys, String> result =
                        requireNonNull(handle.get(SortOrderDialogFragment.ARG_RESULT));
                refreshList(result);
            } else {
                viewModel.simpleRefresh();
            }
        });

        handle.<EnumMap<SortOrderDialogFragment.ResultKeys, String>>getLiveData(SortOrderDialogFragment.ARG_RESULT)
                .observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                refreshList(result);
            }
        });
    }

    private void setupObservers() {
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()){
                Snackbar.make(binding.getRoot(), message, BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });

        viewModel.getStoryList().observe(getViewLifecycleOwner(), stories -> adapter.submitList(stories));
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

    private void refreshList(@NotNull EnumMap<SortOrderDialogFragment.ResultKeys, String> result) {
        viewModel.refreshWithNewParameters(result.get(SortOrderDialogFragment.ResultKeys.SORT),
                result.get(SortOrderDialogFragment.ResultKeys.FILTER));
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
                controller.navigate(SingleFeedFragmentDirections
                        .actionSingleFeedStoryFragmentToSortFilterDialog());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStoryClick(int position) {
        List<Story> storyList = requireNonNull(adapter.getCurrentList());
        Story[] stories = new Story[storyList.size()];
        NavHostFragment.findNavController(this).navigate(SingleFeedFragmentDirections
                .actionSingleFeedStoryFragmentToStoryPagerActivity(storyList.toArray(stories))
                .setInitialStory(position));
    }
}