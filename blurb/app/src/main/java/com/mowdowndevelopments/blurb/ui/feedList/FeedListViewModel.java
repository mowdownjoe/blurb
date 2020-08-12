package com.mowdowndevelopments.blurb.ui.feedList;

import android.app.Application;
import android.util.Log;

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

public class FeedListViewModel extends AndroidViewModel {
    private static final String TAG = "FeedListViewModel";
    private boolean refreshing = false;
    private MutableLiveData<GetFeedsResponse> feedsResponseData;
    private MutableLiveData<LoadingStatus> status;
    private MutableLiveData<String> errorMessage;

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
        status.postValue(LoadingStatus.LOADING);
        Singletons.getNewsBlurAPI(getApplication()).getFeeds().enqueue(new Callback<GetFeedsResponse>() {
            @Override
            public void onResponse(@NotNull Call<GetFeedsResponse> call, @NotNull Response<GetFeedsResponse> response) {
                if (response.isSuccessful()) {
                    status.postValue(LoadingStatus.DONE);
                    //TODO Fill out success case
                } else {
                    status.postValue(LoadingStatus.ERROR);
                    String errorMsg = getApplication().getString(R.string.http_error, response.code());
                    errorMessage.postValue(errorMsg);
                    Log.e(TAG, "loadFeeds.onResponse: "+errorMsg);
                }
            }

            @Override
            public void onFailure(@NotNull Call<GetFeedsResponse> call, @NotNull Throwable t) {
                status.postValue(LoadingStatus.ERROR);
                errorMessage.postValue(t.getLocalizedMessage());
                Log.e(TAG, "loadFeeds.onFailure: "+t.getMessage(), t);
            }
        });
    }

    public void refreshFeeds(){
        if (refreshing) return;
        refreshing = true;
        Singletons.getNewsBlurAPI().getFeedsAndRefreshCounts().enqueue(new Callback<GetFeedsResponse>() {
            @Override
            public void onResponse(@NotNull Call<GetFeedsResponse> call, @NotNull Response<GetFeedsResponse> response) {
                refreshing = false;
                if (response.isSuccessful()){
                    //TODO Fill out success case
                } else {
                    String errorMsg = getApplication().getString(R.string.http_error, response.code());
                    errorMessage.postValue(errorMsg);
                    Log.e(TAG, "loadFeeds.onResponse: "+errorMsg);
                }
            }

            @Override
            public void onFailure(@NotNull Call<GetFeedsResponse> call, @NotNull Throwable t) {
                refreshing = false;
                errorMessage.postValue(t.getLocalizedMessage());
                Log.e(TAG, "loadFeeds.onFailure: "+t.getMessage(), t);
            }
        });
    }
}
