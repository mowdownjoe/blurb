package com.mowdowndevelopments.blurb.ui.feeds.single;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.SingleFeedStoryFragmentArgs;
import com.mowdowndevelopments.blurb.SingleFeedStoryFragmentDirections;
import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.databinding.SingleFeedFragmentBinding;
import com.mowdowndevelopments.blurb.ui.dialogs.SortOrderDialogFragment;
import com.mowdowndevelopments.blurb.ui.feeds.StoryClickListener;
import com.squareup.picasso.Picasso;

import java.util.EnumMap;
import java.util.Objects;

public class SingleFeedFragment extends Fragment implements StoryClickListener {

    SingleFeedFragmentBinding binding;
    private SingleFeedViewModel viewModel;
    private SingleFeedStoryFragmentArgs args;
    private SingleFeedAdapter adapter;

    public static SingleFeedFragment newInstance() {
        return new SingleFeedFragment();
    }

    //TODO Dialog to change sort/filter?

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        args = SingleFeedStoryFragmentArgs.fromBundle(requireArguments());
        Feed feed = args.getFeedToShow();
        adapter = new SingleFeedAdapter(feed, this);
        Picasso.get().load(feed.getFavIconUrl()).fetch(); //Warm up Picasso's cache.
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SingleFeedFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SingleFeedViewModel.class);
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()){
                Snackbar.make(binding.getRoot(), message, BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
        //TODO Observe PagedList to populate adapter
        viewModel.getLoadingStatus().observe(getViewLifecycleOwner(), loadingStatus -> {
            switch (loadingStatus){
                //TODO Observe Loading Status
                case LOADING:
                    break;
                case WAITING:
                case DONE:
                    break;
                case ERROR:
                    break;
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel.loadStories(args.getFeedToShow());

        NavController controller = NavHostFragment.findNavController(this);
        SavedStateHandle handle = Objects.requireNonNull(controller.getCurrentBackStackEntry())
                .getSavedStateHandle();

        handle.getLiveData(SortOrderDialogFragment.ARG_RESULT).observe(getViewLifecycleOwner(), result -> {
            if (!(result instanceof EnumMap)) return;
            EnumMap<SortOrderDialogFragment.ResultKeys, String> resultMap =
                    (EnumMap<SortOrderDialogFragment.ResultKeys, String>) result;
            viewModel.loadStories(args.getFeedToShow(),
                    resultMap.get(SortOrderDialogFragment.ResultKeys.SORT),
                    resultMap.get(SortOrderDialogFragment.ResultKeys.FILTER));
        });

        binding.srfRefreshTab.setOnRefreshListener(() -> {
            if (handle.contains(SortOrderDialogFragment.ARG_RESULT)){
                EnumMap<SortOrderDialogFragment.ResultKeys, String> dialogSortMethod = Objects
                        .requireNonNull(handle.get(SortOrderDialogFragment.ARG_RESULT));
                viewModel.loadStories(args.getFeedToShow(),
                        dialogSortMethod.get(SortOrderDialogFragment.ResultKeys.SORT),
                        dialogSortMethod.get(SortOrderDialogFragment.ResultKeys.FILTER));
            } else {
                viewModel.loadStories(args.getFeedToShow());
            }
        });
        binding.srfRefreshTab.setProgressBackgroundColorSchemeResource(R.color.secondaryColor);
    }

    @Override
    public void onStoryClick(int position) {
        //TODO change when paging is set up
        NavHostFragment.findNavController(this).navigate(SingleFeedStoryFragmentDirections
                .actionSingleFeedStoryFragmentToStoryPagerActivity(viewModel.getFeedData().getValue().getStories())
                .setInitialStory(position));
    }
}