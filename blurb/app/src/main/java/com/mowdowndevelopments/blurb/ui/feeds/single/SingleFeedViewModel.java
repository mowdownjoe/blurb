package com.mowdowndevelopments.blurb.ui.feeds.single;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.network.LoadingStatus;
import com.mowdowndevelopments.blurb.network.ResponseModels.FeedContentsResponse;
import com.mowdowndevelopments.blurb.network.Singletons;
import com.mowdowndevelopments.blurb.ui.feeds.BaseFeedViewModel;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class SingleFeedViewModel extends BaseFeedViewModel {
    public SingleFeedViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadStories(Feed feed){
        SharedPreferences prefs = getApplication()
                .getSharedPreferences(getApplication().getString(R.string.shared_pref_file), 0);
        String sortOrder = prefs.getString(getApplication().getString(R.string.pref_sort_key), "newest");
        String readFilter = prefs.getString(getApplication().getString(R.string.pref_filter_key), "unread");
        loadStories(feed, sortOrder, readFilter);
    }

    public void loadStories(Feed feed, String sortOrder, String readFilter){
        setLoadingStatus(LoadingStatus.LOADING);
        Singletons.getNewsBlurAPI(getApplication()).getFeedContents(feed.getId(), readFilter, sortOrder)
                .enqueue(new Callback<FeedContentsResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<FeedContentsResponse> call, @NotNull Response<FeedContentsResponse> response) {
                        if (response.isSuccessful()){
                            setLoadingStatus(LoadingStatus.DONE);
                            setFeedData(response.body());
                        } else {
                            setLoadingStatus(LoadingStatus.ERROR);
                            String errorMsg = getApplication().getString(R.string.http_error, response.code());
                            setErrorMessage(errorMsg);
                            Timber.e("SingleFeedViewModel.loadStories.onResponse: %s", errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<FeedContentsResponse> call, @NotNull Throwable t) {
                        setLoadingStatus(LoadingStatus.ERROR);
                        setErrorMessage(t.getLocalizedMessage());
                        Timber.e(t, "SingleFeedViewModel.loadStories.onFailure: %s", t.getMessage());
                        FirebaseCrashlytics.getInstance().recordException(t);
                    }
                });
    }
}