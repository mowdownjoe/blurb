package com.mowdowndevelopments.blurb.ui.navHost;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.network.LoadingStatus;
import com.mowdowndevelopments.blurb.network.Singletons;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends AndroidViewModel {


    private static final String TAG = "MainViewModel";
    private MutableLiveData<LoadingStatus> logoutStatus;
    private MutableLiveData<String> errorMessage;

    public MainViewModel(@NonNull Application application) {
        super(application);
        errorMessage = new MutableLiveData<>();
        logoutStatus = new MutableLiveData<>(LoadingStatus.WAITING);
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<LoadingStatus> getLogoutStatus() {
        return logoutStatus;
    }

    public void logout(){
        logoutStatus.postValue(LoadingStatus.LOADING);
        Singletons.getNewsBlurAPI(getApplication()).logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                if (response.isSuccessful()){
                    logoutStatus.postValue(LoadingStatus.DONE);
                    SharedPreferences prefs = getApplication()
                            .getSharedPreferences(getApplication().getString(R.string.shared_pref_file), 0);
                    prefs.edit().putBoolean(getApplication().getString(R.string.logged_in_key), false).apply();
                } else {
                    logoutStatus.postValue(LoadingStatus.ERROR);
                    //TODO Add messages to explain HTTP Error codes in plain language
                    String errorMsg = getApplication().getString(R.string.http_error, response.code());
                    errorMessage.postValue(errorMsg);
                    Log.e(TAG, "onResponse: "+errorMsg);
                }
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                logoutStatus.postValue(LoadingStatus.ERROR);
                Log.e(TAG, "onFailure: "+t.getMessage(), t);
                errorMessage.postValue(t.getLocalizedMessage());
            }
        });
    }
}
