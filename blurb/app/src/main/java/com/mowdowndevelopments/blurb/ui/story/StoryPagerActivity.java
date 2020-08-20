package com.mowdowndevelopments.blurb.ui.story;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ActivityNavigator;

import com.mowdowndevelopments.blurb.databinding.StoryPagerActivityBinding;

import java.util.Objects;

public class StoryPagerActivity extends AppCompatActivity {

    StoryPagerActivityBinding binding;
    StoryPagerActivityArgs args;
    StoryViewModel viewModel;

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

        binding.fab.setOnLongClickListener(view -> {
            if (viewModel.getActiveStory() != null) {
                Intent intent = new Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_TITLE, viewModel.getActiveStory().getTitle())
                        .putExtra(Intent.EXTRA_TEXT, viewModel.getActiveStory().getPermalink());
                startActivity(Intent.createChooser(intent, null));
                return true;
            }
            return false;
        });

        if (binding.guideMidline != null){
            binding.vp2StoryPager.setUserInputEnabled(false);
        }
    }

    private void setUpViewModelObservers(){
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