package com.mowdowndevelopments.blurb.ui.story;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mowdowndevelopments.blurb.database.entities.Story;

public class StoryViewModel extends ViewModel {

    private MutableLiveData<Story[]> stories;
    private MutableLiveData<Integer> indexToView;
    private Story activeStory = null;

    public StoryViewModel() {
        stories = new MutableLiveData<>();
        indexToView = new MutableLiveData<>();
    }

    public Story getActiveStory() {
        return activeStory;
    }

    public void setActiveStory(Story activeStory) {
        this.activeStory = activeStory;
    }

    public LiveData<Story[]> getStories() {
        return stories;
    }

    public void setStories(Story[] newStories) {
        stories.setValue(newStories);
    }

    public LiveData<Integer> getIndexToView() {
        return indexToView;
    }

    public void setIndexToView(int newIndex) {
        indexToView.setValue(newIndex);
    }

    // TODO: Implement the ViewModel
}