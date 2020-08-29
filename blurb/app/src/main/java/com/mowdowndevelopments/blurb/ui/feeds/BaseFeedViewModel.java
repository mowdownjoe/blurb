package com.mowdowndevelopments.blurb.ui.feeds;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mowdowndevelopments.blurb.network.LoadingStatus;
import com.mowdowndevelopments.blurb.network.ResponseModels.FeedContentsResponse;

public class BaseFeedViewModel extends AndroidViewModel {
    private MutableLiveData<FeedContentsResponse> feedData;
    private MutableLiveData<LoadingStatus> loadingStatus;
    private MutableLiveData<String> errorMessage;

    public BaseFeedViewModel(@NonNull Application application) {
        super(application);
        feedData = new MutableLiveData<>();
        loadingStatus = new MutableLiveData<>(LoadingStatus.WAITING);
        errorMessage = new MutableLiveData<>();
    }

    public LiveData<FeedContentsResponse> getFeedData() {
        return feedData;
    }

    public LiveData<LoadingStatus> getLoadingStatus() {
        return loadingStatus;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String message) {
        errorMessage.postValue(message);
    }

    public void setFeedData(FeedContentsResponse data) {
        feedData.postValue(data);
    }

    public void setLoadingStatus(LoadingStatus status){
        loadingStatus.postValue(status);
    }
}
