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
import com.mowdowndevelopments.blurb.network.Singletons;
import com.mowdowndevelopments.blurb.network.responseModels.FeedContentsResponse;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static java.util.Objects.requireNonNull;

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
        pageLoadingStatus = new MutableLiveData<>(LoadingStatus.WAITING);
        initialLoadingStatus = new MutableLiveData<>(LoadingStatus.WAITING);
    }


    public void resetWithNewParameters(@NonNull String newSortOrder, @NonNull String newFilter){
        sortOrder = newSortOrder;
        filter = newFilter;
        invalidate();
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Integer, Story> callback) {
        initialLoadingStatus.postValue(LoadingStatus.LOADING);
        Singletons.getNewsBlurAPI(context).getFeedContents(feedId, filter, sortOrder)
                .enqueue(new Callback<FeedContentsResponse>() {
            @Override
            public void onResponse(@NotNull Call<FeedContentsResponse> call, @NotNull Response<FeedContentsResponse> response) {
                Timber.d("Successfully received response. Response Code: %o", response.code());
                if (response.isSuccessful()) {
                    initialLoadingStatus.postValue(LoadingStatus.DONE);
                    FeedContentsResponse body = requireNonNull(response.body());
                    callback.onResult(Arrays.asList(body.getStories()), null, 2);
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
        });
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Story> callback) {
        pageLoadingStatus.postValue(LoadingStatus.LOADING);
        Singletons.getNewsBlurAPI(context).getFeedContents(feedId, filter, sortOrder, params.key)
                .enqueue(new Callback<FeedContentsResponse>() {
            @Override
            public void onResponse(@NotNull Call<FeedContentsResponse> call, @NotNull Response<FeedContentsResponse> response) {
                Timber.d("Successfully received response. Response Code: %o", response.code());
                if (response.isSuccessful()){
                    pageLoadingStatus.postValue(LoadingStatus.DONE);
                    Story[] stories = requireNonNull(response.body()).getStories();
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
        });
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
        private MutableLiveData<SingleFeedDataSource> mostRecentDataSource;

        public Factory(Context context, int feedId, String sortOrder, String filter) {
            this.context = context;
            this.feedId = feedId;
            this.sortOrder = sortOrder;
            this.filter = filter;
            mostRecentDataSource = new MutableLiveData<>();
        }

        public LiveData<SingleFeedDataSource> getMostRecentDataSource() {
            return mostRecentDataSource;
        }

        @NonNull
        @Override
        public SingleFeedDataSource create() {
            SingleFeedDataSource dataSource = new SingleFeedDataSource(context, feedId, sortOrder, filter);
            mostRecentDataSource.postValue(dataSource);
            return dataSource;
        }
    }
}
