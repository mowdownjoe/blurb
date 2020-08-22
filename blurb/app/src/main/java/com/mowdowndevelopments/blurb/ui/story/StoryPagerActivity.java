package com.mowdowndevelopments.blurb.ui.story;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ActivityNavigator;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.databinding.StoryPagerActivityBinding;

import java.util.Objects;

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
        viewModel.getStories().observe(this, stories -> { //Observer should only be called once.
            if (stories != null) {
                viewModel.getIndexToView().observe(this, this::setPage);
                binding.vp2StoryPager.setAdapter(new StoryPagerAdapter(this, stories));
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