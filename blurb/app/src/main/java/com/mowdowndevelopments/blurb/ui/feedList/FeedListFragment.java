package com.mowdowndevelopments.blurb.ui.feedList;

import android.content.SharedPreferences;
import android.os.Build;
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
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.databinding.FragmentFeedListBinding;
import com.mowdowndevelopments.blurb.ui.dialogs.NewFeedDialogFragment;
import com.mowdowndevelopments.blurb.ui.dialogs.NewFolderDialogFragment;
import com.mowdowndevelopments.blurb.ui.login.LoginFragment;
import com.mowdowndevelopments.blurb.work.FetchStarredStoriesWorker;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static java.util.Objects.requireNonNull;

public class FeedListFragment extends Fragment implements FeedListAdapter.ItemOnClickListener {

    FragmentFeedListBinding binding;
    FeedListViewModel viewModel;
    FeedListAdapter adapter;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Timber.d("Lifecycle: Creating view");
        binding = FragmentFeedListBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Timber.d("Lifecycle: View created");

        binding.srfRefreshTab.setProgressBackgroundColorSchemeResource(R.color.secondaryColor);
        binding.srfRefreshTab.setOnRefreshListener(() -> viewModel.refreshFeeds());

        viewModel = new ViewModelProvider(this).get(FeedListViewModel.class);
        viewModel.getFeedsResponseData().observe(getViewLifecycleOwner(), getFeedsResponse -> {
            if (getFeedsResponse != null){
                adapter.setData(getFeedsResponse);
                binding.srfRefreshTab.setRefreshing(false);
                viewModel.postFeedsToDb(getFeedsResponse);
            }
        });
        viewModel.getLoadingStatus().observe(getViewLifecycleOwner(), loadingStatus -> {
            switch (loadingStatus){
                case LOADING:
                    binding.pbLoadingSpinner.setVisibility(View.VISIBLE);
                    binding.rvFeedList.setVisibility(View.INVISIBLE);
                    binding.tvErrorText.setVisibility(View.INVISIBLE);
                    break;
                case WAITING:
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

        adapter = new FeedListAdapter(this);
        binding.rvFeedList.setAdapter(adapter);
        binding.rvFeedList.setHasFixedSize(true);
        binding.rvFeedList.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (requireActivity().getSharedPreferences(getString(R.string.shared_pref_file), 0)
                .getBoolean(getString(R.string.logged_in_key), false)){
            viewModel.loadFeeds();
        }

        SavedStateHandle handle = requireNonNull(NavHostFragment.findNavController(this)
                .getCurrentBackStackEntry()).getSavedStateHandle();
        handle.<Boolean>getLiveData(LoginFragment.LOGIN_SUCCESS).observe(getViewLifecycleOwner(), loggedIn -> {
            if (loggedIn){
                viewModel.loadFeeds();

                Constraints.Builder constraintBuilder = new Constraints.Builder()
                        .setRequiresCharging(true)
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresStorageNotLow(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    constraintBuilder.setRequiresDeviceIdle(true);
                }
                PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(FetchStarredStoriesWorker.class, 24, TimeUnit.HOURS)
                        .setConstraints(constraintBuilder.build())
                        .addTag(FetchStarredStoriesWorker.WORK_TAG)
                        .build();

                WorkManager.getInstance(requireContext()).enqueue(request);

            }
        });
        handle.<EnumMap<NewFolderDialogFragment.ResultKeys, String>>getLiveData(NewFolderDialogFragment.ARG_DIALOG_RESULT)
                .observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                if (result.containsKey(NewFolderDialogFragment.ResultKeys.NESTED_UNDER)){
                    viewModel.createNewFolder(result.get(NewFolderDialogFragment.ResultKeys.NEW_FOLDER),
                            result.get(NewFolderDialogFragment.ResultKeys.NESTED_UNDER));
                } else {
                    viewModel.createNewFolder(result.get(NewFolderDialogFragment.ResultKeys.NEW_FOLDER));
                }
            }
        });
        handle.<EnumMap<NewFeedDialogFragment.ResultKeys, String>>getLiveData(NewFeedDialogFragment.ARG_RESULT)
                .observe(getViewLifecycleOwner(), result -> {
            if (result != null){
                if (result.containsKey(NewFeedDialogFragment.ResultKeys.FOLDER)){
                    viewModel.addNewFeed(result.get(NewFeedDialogFragment.ResultKeys.FEED),
                            result.get(NewFeedDialogFragment.ResultKeys.FOLDER));
                } else {
                    viewModel.addNewFeed(result.get(NewFeedDialogFragment.ResultKeys.FEED));
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.feed_list_menu, menu);
        Timber.d("Inflated options menu.");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        NavController navController = NavHostFragment.findNavController(this);
        Set<String> folders = requireNonNull(viewModel.getFeedsResponseData().getValue())
                .getFolders().keySet();
        String[] folderArray = new String[folders.size()];
        switch (item.getItemId()){
            case R.id.mi_add_feed:
                navController.navigate(FeedListFragmentDirections.actionFeedListFragmentToAddFeedDialog()
                        .setFolderNames(folders.toArray(folderArray)));
                return true;
            case R.id.mi_new_folder:
                navController.navigate(FeedListFragmentDirections.actionFeedListFragmentToNewFolderDialog()
                        .setFolderNames(folders.toArray(folderArray)));
                return true;
            case R.id.mi_view_all_feeds:
                Collection<Feed> feeds = viewModel.getFeedsResponseData().getValue().getFeeds().values();
                Feed[] feedArray = new Feed[feeds.size()];
                navController.navigate(FeedListFragmentDirections
                        .actionFeedListFragmentToRiverOfNewsFragment(feeds.toArray(feedArray)));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFeedItemClick(Feed f) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(getString(R.string.shared_pref_file), 0);
        if (prefs.getBoolean(getString(R.string.pref_analytics_key), false)) {
            Bundle bundle = new Bundle();
            bundle.putInt(FirebaseAnalytics.Param.QUANTITY, adapter.getFeedCount());
            bundle.putString(FirebaseAnalytics.Param.DESTINATION, f.getFeedAddress());
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, f.getFeedTitle());
            FirebaseAnalytics.getInstance(requireContext())
                    .logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }

        NavHostFragment.findNavController(this).navigate(FeedListFragmentDirections
                .actionFeedListFragmentToSingleFeedStoryFragment(f));
    }

    @Override
    public void onFolderItemClick(Folder f) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(getString(R.string.shared_pref_file), 0);
        if (prefs.getBoolean(getString(R.string.pref_analytics_key), false)) {
            Bundle bundle = new Bundle();
            bundle.putInt(FirebaseAnalytics.Param.QUANTITY, adapter.getFeedCount());
            FirebaseAnalytics.getInstance(requireContext())
                    .logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }

        Feed[] feeds = new Feed[f.getFeeds().size()];
        NavHostFragment.findNavController(this).navigate(FeedListFragmentDirections
                .actionFeedListFragmentToRiverOfNewsFragment(f.getFeeds().toArray(feeds)));
    }
}