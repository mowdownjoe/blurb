package com.mowdowndevelopments.blurb.ui.feeds.single;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.network.LoadingStatus;
import com.mowdowndevelopments.blurb.ui.feeds.BaseFeedViewModel;

public class SingleFeedViewModel extends BaseFeedViewModel {

    final private Observer<LoadingStatus> statusObserver = this::setLoadingStatus;
    final private Observer<String> errorMessageObserver = this::setErrorMessage;
    private SingleFeedDataSource mostRecentDataSource;
    private LiveData<PagedList<Story>> storyList;

    public LiveData<PagedList<Story>> getStoryList() {
        return storyList;
    }

    public SingleFeedViewModel(@NonNull Application app, int feedId) { //TODO Modify constructor to pass in feed ID
        super(app);
        SharedPreferences prefs = app.getSharedPreferences(app.getString(R.string.shared_pref_file), 0);
        String sortOrder = prefs.getString(app.getString(R.string.pref_filter_key), "newest");
        String filter = prefs.getString(app.getString(R.string.pref_sort_key), "all");
        SingleFeedDataSourceFactory factory = new SingleFeedDataSourceFactory(
                getApplication(),
                feedId,
                sortOrder,
                filter
        );
        mostRecentDataSource = factory.create();
        storyList = new LivePagedListBuilder<>(factory, 6).build();
        setInternalObservers();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cleanUpObservers();
    }

    private void setInternalObservers(){
        mostRecentDataSource.getErrorMessage().observeForever(errorMessageObserver);
        mostRecentDataSource.getLoadingStatus().observeForever(statusObserver);
    }

    private void cleanUpObservers() {
        mostRecentDataSource.getErrorMessage().removeObserver(errorMessageObserver);
        mostRecentDataSource.getLoadingStatus().removeObserver(statusObserver);
    }

    public void simpleRefresh(){
        mostRecentDataSource.invalidate();
    }

    public void refreshWithNewParameters(Feed feed, String sortOrder, String filter){
        cleanUpObservers();
        SingleFeedDataSourceFactory factory = new SingleFeedDataSourceFactory(
                getApplication(),
                feed.getId(),
                sortOrder,
                filter
        );
        mostRecentDataSource = factory.create();
        setInternalObservers();
    }

    static class Factory extends ViewModelProvider.AndroidViewModelFactory{

        private int feedId;
        private Application app;
        public Factory(@NonNull Application application, int feedId) {
            super(application);
            app = application;
            this.feedId = feedId;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SingleFeedViewModel(app, feedId);
        }
    }
}