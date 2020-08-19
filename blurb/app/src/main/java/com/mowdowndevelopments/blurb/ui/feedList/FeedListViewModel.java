package com.mowdowndevelopments.blurb.ui.feedList;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.network.LoadingStatus;
import com.mowdowndevelopments.blurb.network.ResponseModels.GetFeedsResponse;
import com.mowdowndevelopments.blurb.network.Singletons;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class FeedListViewModel extends AndroidViewModel {

    private boolean refreshing = false;
    private MutableLiveData<GetFeedsResponse> feedsResponseData;
    private MutableLiveData<LoadingStatus> status;
    private MutableLiveData<String> errorMessage;

    private Callback<Void> newFeedCallback = new Callback<Void>() {
        @Override
        public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
            refreshing = false;
            if (response.isSuccessful()) {
                refreshFeeds();
            } else {
                String errorMsg = getApplication().getString(R.string.http_error, response.code());
                errorMessage.postValue(errorMsg);
                Timber.e("loadFeeds.onResponse: %s", errorMsg);
            }
        }

        @Override
        public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
            refreshing = false;
            errorMessage.postValue(t.getLocalizedMessage());
            Timber.e(t, "loadFeeds.onFailure: %s", t.getMessage());
        }
    };

    public FeedListViewModel(@NonNull Application application) {
        super(application);
        status = new MutableLiveData<>(LoadingStatus.WAITING);
        feedsResponseData = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
    }

    public LiveData<GetFeedsResponse> getFeedsResponseData() {
        return feedsResponseData;
    }

    public LiveData<LoadingStatus> getLoadingStatus() {
        return status;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadFeeds(){
        if (status.getValue() == LoadingStatus.LOADING) return;
        if (refreshing) return;
        Timber.v("Loading feeds.");
        status.postValue(LoadingStatus.LOADING);
        Singletons.getNewsBlurAPI(getApplication()).getFeeds().enqueue(new Callback<GetFeedsResponse>() {
            @Override
            public void onResponse(@NotNull Call<GetFeedsResponse> call, @NotNull Response<GetFeedsResponse> response) {
                if (response.isSuccessful()) {
                    status.postValue(LoadingStatus.DONE);
                    feedsResponseData.postValue(response.body());
                    Timber.v("Feeds loaded.");
                } else {
                    status.postValue(LoadingStatus.ERROR);
                    String errorMsg = getApplication().getString(R.string.http_error, response.code());
                    errorMessage.postValue(errorMsg);
                    Timber.e("loadFeeds.onResponse: %s", errorMsg);
                }
            }

            @Override
            public void onFailure(@NotNull Call<GetFeedsResponse> call, @NotNull Throwable t) {
                status.postValue(LoadingStatus.ERROR);
                errorMessage.postValue(t.getLocalizedMessage());
                Timber.e(t, "loadFeeds.onFailure: %s", t.getMessage());
            }
        });
    }

    public void refreshFeeds(){
        if (status.getValue() == LoadingStatus.LOADING) return;
        if (refreshing) return;
        refreshing = true;
        Singletons.getNewsBlurAPI(getApplication()).getFeedsAndRefreshCounts().enqueue(new Callback<GetFeedsResponse>() {
            @Override
            public void onResponse(@NotNull Call<GetFeedsResponse> call, @NotNull Response<GetFeedsResponse> response) {
                refreshing = false;
                if (response.isSuccessful()){
                    feedsResponseData.postValue(response.body());
                } else {
                    String errorMsg = getApplication().getString(R.string.http_error, response.code());
                    errorMessage.postValue(errorMsg);
                    Timber.e("loadFeeds.onResponse: %s", errorMsg);
                }
            }

            @Override
            public void onFailure(@NotNull Call<GetFeedsResponse> call, @NotNull Throwable t) {
                refreshing = false;
                errorMessage.postValue(t.getLocalizedMessage());
                Timber.e(t, "loadFeeds.onFailure: %s", t.getMessage());
            }
        });
    }

    public void addNewFeed(String url){
        if (status.getValue() == LoadingStatus.LOADING) return;
        if (refreshing) return;
        refreshing = true;
        Singletons.getNewsBlurAPI(getApplication()).addNewFeed(url).enqueue(newFeedCallback);
    }

    public void addNewFeed(String url, String folder){
        if (status.getValue() == LoadingStatus.LOADING) return;
        if (refreshing) return;
        refreshing = true;
        Singletons.getNewsBlurAPI(getApplication()).addNewFeed(url, folder).enqueue(newFeedCallback);
    }

    public void createNewFolder(String folderName){
        if (status.getValue() == LoadingStatus.LOADING) return;
        if (refreshing) return;
        refreshing = true;
        Singletons.getNewsBlurAPI(getApplication()).createNewFolder(folderName).enqueue(newFeedCallback);
    }

    public void createNewFolder(String folderName, String parentFolderName){
        if (status.getValue() == LoadingStatus.LOADING) return;
        if (refreshing) return;
        refreshing = true;
        Singletons.getNewsBlurAPI(getApplication()).createNewFolder(folderName, parentFolderName).enqueue(newFeedCallback);
    }
}
