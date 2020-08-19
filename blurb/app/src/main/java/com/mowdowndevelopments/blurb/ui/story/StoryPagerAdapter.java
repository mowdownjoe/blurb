package com.mowdowndevelopments.blurb.ui.story;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mowdowndevelopments.blurb.database.entities.Story;

public class StoryPagerAdapter extends FragmentStateAdapter {

    //TODO Set up data for Adapter
    private Story[] stories;

    public StoryPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (stories != null){
            return StoryFragment.newInstance(stories[position]);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        if (stories != null){
            return stories.length;
        }
        return 0;
    }
}
