package com.mowdowndevelopments.blurb.ui.feeds.favorite;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.mowdowndevelopments.blurb.database.BlurbDb;
import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.ui.feeds.BaseFeedViewModel;

import java.util.List;


public class FavoriteStoriesViewModel extends BaseFeedViewModel {
    final LiveData<List<Story>> storyList;

    public FavoriteStoriesViewModel(@NonNull Application app) {
        super(app);
        storyList = BlurbDb.getInstance(app).blurbDao().getStarredStoryList();
    }
}