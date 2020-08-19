package com.mowdowndevelopments.blurb.ui.story;

import androidx.lifecycle.ViewModel;

import com.mowdowndevelopments.blurb.database.entities.Story;

public class StoryViewModel extends ViewModel {

    private Story activeStory;

    public Story getActiveStory() {
        return activeStory;
    }

    public void setActiveStory(Story activeStory) {
        this.activeStory = activeStory;
    }
    // TODO: Implement the ViewModel
}