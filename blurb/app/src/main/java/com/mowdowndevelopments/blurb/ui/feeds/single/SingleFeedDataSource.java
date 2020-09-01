package com.mowdowndevelopments.blurb.ui.feeds.single;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.network.LoadingStatus;
import com.mowdowndevelopments.blurb.network.ResponseModels.FeedContentsResponse;
import com.mowdowndevelopments.blurb.network.Singletons;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class SingleFeedDataSource extends PageKeyedDataSource<Integer, Story> {

    private Context context;
    private int feedId;
    private String sortOrder;
    private String filter;
    private MutableLiveData<LoadingStatus> pageLoadingStatus;
    private MutableLiveData<LoadingStatus> initialLoadingStatus;
    private MutableLiveData<String> errorMessage;

    public LiveData<LoadingStatus> getPageLoadingStatus() {
        return pageLoadingStatus;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<LoadingStatus> getInitialLoadingStatus(){
        return initialLoadingStatus;
    }

    public SingleFeedDataSource(Context context, int id, String sortOrder, String filter) {
        super();
        this.context = context;
        feedId = id;
        this.sortOrder = sortOrder;
        this.filter = filter;
        errorMessage = new MutableLiveData<>();
        pageLoadingStatus = new MutableLiveData<>();
        initialLoadingStatus = new MutableLiveData<>();
    }


    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Integer, Story> callback) {
        initialLoadingStatus.setValue(LoadingStatus.LOADING);
        Callback<FeedContentsResponse> responseCallback = new Callback<FeedContentsResponse>() {
            @Override
            public void onResponse(@NotNull Call<FeedContentsResponse> call, @NotNull Response<FeedContentsResponse> response) {
                if (response.isSuccessful()) {
                    callback.onResult(Arrays.asList(response.body().getStories()), null, 2);
                    initialLoadingStatus.postValue(LoadingStatus.DONE);
                } else {
                    initialLoadingStatus.postValue(LoadingStatus.ERROR);
                    errorMessage.postValue(context.getString(R.string.http_error, response.code()));
                }
            }

            @Override
            public void onFailure(@NotNull Call<FeedContentsResponse> call, @NotNull Throwable t) {
                initialLoadingStatus.postValue(LoadingStatus.ERROR);
                errorMessage.postValue(t.getLocalizedMessage());
                FirebaseCrashlytics.getInstance().recordException(t);
                Timber.e(t);
            }
        };
        Singletons.getNewsBlurAPI(context).getFeedContents(feedId, filter, sortOrder).enqueue(responseCallback);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Story> callback) {
        pageLoadingStatus.postValue(LoadingStatus.LOADING);
        Callback<FeedContentsResponse> responseCallback = new Callback<FeedContentsResponse>() {
            @Override
            public void onResponse(@NotNull Call<FeedContentsResponse> call, @NotNull Response<FeedContentsResponse> response) {
                if (response.isSuccessful()){
                    pageLoadingStatus.postValue(LoadingStatus.DONE);
                    Story[] stories = response.body().getStories();
                    if (stories.length > 0) {
                        callback.onResult(Arrays.asList(stories), params.key +1);
                    } else {
                        callback.onResult(Arrays.asList(stories), null);
                    }
                } else {
                    pageLoadingStatus.postValue(LoadingStatus.ERROR);
                    errorMessage.postValue(context.getString(R.string.http_error, response.code()));
                }
            }

            @Override
            public void onFailure(@NotNull Call<FeedContentsResponse> call, @NotNull Throwable t) {
                pageLoadingStatus.postValue(LoadingStatus.ERROR);
                errorMessage.postValue(t.getLocalizedMessage());
                FirebaseCrashlytics.getInstance().recordException(t);
                Timber.e(t);
            }
        };
        Singletons.getNewsBlurAPI(context).getFeedContents(feedId, filter, sortOrder, params.key).enqueue(responseCallback);
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Story> callback) {
        //Will only append to initial load, not prepend
    }

    static class Factory extends DataSource.Factory<Integer, Story> {

        private Context context;
        private int feedId;
        private String sortOrder;
        private String filter;

        public Factory(Context context, int feedId, String sortOrder, String filter) {
            this.context = context;
            this.feedId = feedId;
            this.sortOrder = sortOrder;
            this.filter = filter;
        }

        @NonNull
        @Override
        public SingleFeedDataSource create() {
            return new SingleFeedDataSource(context, feedId, sortOrder, filter);
        }
    }
}