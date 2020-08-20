package com.mowdowndevelopments.blurb.ui.story;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.network.Singletons;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoryViewModel extends AndroidViewModel {

    private MutableLiveData<Story[]> stories;
    private MutableLiveData<Integer> indexToView;
    private Story activeStory = null;

    public StoryViewModel(Application app) {
        super(app);
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

    public void markStoryAsUnread(String storyHash){
        Singletons.getNewsBlurAPI(getApplication()).markStoryAsUnread(storyHash).enqueue(new Callback<Void>() {
            //TODO Fill out callback

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }
    // TODO: Implement the ViewModel
}