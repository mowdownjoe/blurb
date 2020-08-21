package com.mowdowndevelopments.blurb.ui.story;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ActivityNavigator;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.databinding.StoryPagerActivityBinding;

import java.util.Objects;

import timber.log.Timber;

public class StoryPagerActivity extends AppCompatActivity {

    StoryPagerActivityBinding binding;
    StoryPagerActivityArgs args;
    StoryViewModel viewModel; //ViewModel shared between activity and fragments.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = StoryPagerActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        args = StoryPagerActivityArgs.fromBundle(Objects.requireNonNull(getIntent().getExtras()));
        viewModel = new ViewModelProvider(this).get(StoryViewModel.class);

        setUpViewModelObservers();
        viewModel.setStories(args.getStories());

        binding.fab.setOnClickListener(view -> {
            try {
                boolean isStarred = viewModel.getIsActiveStoryStarred().getValue();
                if (isStarred){
                    viewModel.removeStoryFromStarred(viewModel.getActiveStory());
                } else {
                    viewModel.markStoryAsStarred(viewModel.getActiveStory());
                }
            } catch (NullPointerException e){
                Timber.e(e, "ViewModel has not initialized isActiveStoryStarred.");
            }
        });
        binding.fab.setOnLongClickListener(view -> {
            try {
                boolean isStarred = viewModel.getIsActiveStoryStarred().getValue();
                if (isStarred){
                    Toast.makeText(this, R.string.fab_remove_star, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.fab_add_star, Toast.LENGTH_SHORT).show();
                }
                return true;
            } catch (NullPointerException e) {
                Timber.e(e, "ViewModel has not initialized isActiveStoryStarred.");
                return false;
            }
        });

        if (binding.guideMidline != null){
            binding.vp2StoryPager.setUserInputEnabled(false);
            binding.toolbar.setTitle(R.string.dest_stories);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.markQueueAsRead();
    }

    private void setUpViewModelObservers(){
        viewModel.getSnackbarMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()){
                Snackbar.make(binding.getRoot(), message, BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
        viewModel.getIsActiveStoryStarred().observe(this, isStarred -> {
            if (isStarred){
                binding.fab.setImageResource(R.drawable.ic_unfavorite);
            } else {
                binding.fab.setImageResource(R.drawable.ic_favorite);
            }
        });
        viewModel.getStories().observe(this, stories -> { //Observer should only be called once.
            if (stories != null) {
                binding.vp2StoryPager.setAdapter(new StoryPagerAdapter(this, stories));
                viewModel.getIndexToView().observe(this, this::setPage);
                viewModel.setIndexToView(args.getInitialStory());
            }
        });
    }

    private void setPage(Integer storyIndex) { //Will be called multiple times in multi-pane.
        if (storyIndex != null){
            binding.vp2StoryPager.setCurrentItem(storyIndex, false);
        }
    }

    @Override
    public void finish() {
        ActivityNavigator.applyPopAnimationsToPendingTransition(this);
        super.finish();
    }
}