package com.mowdowndevelopments.blurb.ui.story;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mowdowndevelopments.blurb.database.entities.Story;

public class StoryPagerAdapter extends FragmentStateAdapter {

    //TODO Set up data for Adapter
    private Story[] stories;

    public StoryPagerAdapter(@NonNull FragmentActivity fragmentActivity, Story[] stories) {
        super(fragmentActivity);
        this.stories = stories;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (stories != null){
            return StoryFragment.newInstance(stories[position]);
        }
        throw new IllegalStateException("Attempted to create a fragment from a null set of stories.");
    }

    @Override
    public int getItemCount() {
        if (stories != null){
            return stories.length;
        }
        return 0;
    }
}
