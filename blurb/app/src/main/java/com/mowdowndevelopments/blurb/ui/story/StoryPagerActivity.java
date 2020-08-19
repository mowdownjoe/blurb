package com.mowdowndevelopments.blurb.ui.story;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.mowdowndevelopments.blurb.databinding.StoryPagerActivityBinding;

public class StoryPagerActivity extends AppCompatActivity {

    StoryPagerActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = StoryPagerActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, StoryFragment.newInstance())
                    .commitNow();
        }*/
    }
}