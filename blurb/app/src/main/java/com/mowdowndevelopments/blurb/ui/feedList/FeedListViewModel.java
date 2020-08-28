package com.mowdowndevelopments.blurb.ui.feedList;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.mowdowndevelopments.blurb.AppExecutors;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.BlurbDb;
import com.mowdowndevelopments.blurb.network.LoadingStatus;
import com.mowdowndevelopments.blurb.network.ResponseModels.GetFeedsResponse;
import com.mowdowndevelopments.blurb.network.Singletons;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class FeedListViewModel extends AndroidViewModel {

    private boolean refreshing = false;
    private MutableLiveData<GetFeedsResponse> feedsResponseData;
    private MutableLiveData<LoadingStatus> status;
    private MutableLiveData<String> errorMessage;

    private Callback<Map<String, Object>> newFeedCallback = new Callback<Map<String, Object>>() {
        @Override
        public void onResponse(@NotNull Call<Map<String, Object>> call, @NotNull Response<Map<String, Object>> response) {
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
        public void onFailure(@NotNull Call<Map<String, Object>> call, @NotNull Throwable t) {
            refreshing = false;
            errorMessage.postValue(t.getLocalizedMessage());
            FirebaseCrashlytics.getInstance().log(String.format("loginCallback.onFailure: %s", t.getMessage()));
            FirebaseCrashlytics.getInstance().recordException(t);
        }
    };

    public FeedListViewModel(@NonNull Application application) {
        super(application);
        status = new MutableLiveData<>(LoadingStatus.WAITING);
        feedsResponseData = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
    }

    @NonNull
    public LiveData<GetFeedsResponse> getFeedsResponseData() {
        return feedsResponseData;
    }

    @NonNull
    public LiveData<LoadingStatus> getLoadingStatus() {
        return status;
    }

    @NonNull
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadFeeds(){
        if (status.getValue() == LoadingStatus.LOADING) return;
        if (refreshing) return;
        Timber.d("Loading feeds.");
        status.postValue(LoadingStatus.LOADING);
        Singletons.getNewsBlurAPI(getApplication()).getFeeds().enqueue(new Callback<GetFeedsResponse>() {
            @Override
            public void onResponse(@NotNull Call<GetFeedsResponse> call, @NotNull Response<GetFeedsResponse> response) {
                if (response.isSuccessful()) {
                    status.postValue(LoadingStatus.DONE);
                    feedsResponseData.postValue(response.body());
                    Timber.d("Feeds loaded.");
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
                FirebaseCrashlytics.getInstance().log(String.format("loginCallback.onFailure: %s", t.getMessage()));
                FirebaseCrashlytics.getInstance().recordException(t);
            }
        });
    }

    public void postFeedsToDb() {
        @NotNull GetFeedsResponse response = Objects.requireNonNull(feedsResponseData.getValue());
        postFeedsToDb(response);
    }

    public void postFeedsToDb(GetFeedsResponse feedData){
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                BlurbDb.getInstance(getApplication()).blurbDao().addFeeds(feedData.getFeeds().values());
            } catch (Exception e) {
                Timber.w(e);
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
