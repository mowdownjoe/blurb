package com.mowdowndevelopments.blurb.ui.feedList;

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
import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.databinding.FragmentFeedListBinding;
import com.mowdowndevelopments.blurb.ui.login.LoginFragment;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public class FeedListFragment extends Fragment implements FeedListAdapter.ItemOnClickListener {

    FragmentFeedListBinding binding;
    FeedListAdapter adapter;
    FeedListViewModel viewModel;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFeedListBinding.inflate(inflater, container, false);
        binding.srfRefreshTab.setProgressBackgroundColorSchemeResource(R.color.secondaryColor);
        binding.srfRefreshTab.setOnRefreshListener(() -> viewModel.refreshFeeds());

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
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new FeedListAdapter(this);
        binding.rvFeedList.setAdapter(adapter);
        binding.rvFeedList.setHasFixedSize(true);
        binding.rvFeedList.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (requireActivity().getSharedPreferences(getString(R.string.shared_pref_file), 0)
                .getBoolean(getString(R.string.logged_in_key), false)){
            viewModel.loadFeeds();
        }

        SavedStateHandle handle = Objects.requireNonNull(NavHostFragment.findNavController(this)
                .getCurrentBackStackEntry()).getSavedStateHandle();
        handle.getLiveData(LoginFragment.LOGIN_SUCCESS).observe(getViewLifecycleOwner(), loggedIn -> {
            if (Boolean.TRUE.equals(loggedIn)){ //LiveData returned by handle is of generic type, so must be checked.
                viewModel.loadFeeds();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.feed_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        NavController navController = NavHostFragment.findNavController(this);
        Set<String> folders = Objects.requireNonNull(viewModel.getFeedsResponseData().getValue())
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
        }
        return super.onOptionsItemSelected(item);
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