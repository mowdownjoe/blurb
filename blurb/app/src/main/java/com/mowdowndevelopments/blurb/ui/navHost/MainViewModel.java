package com.mowdowndevelopments.blurb.ui.navHost;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.network.LoadingStatus;
import com.mowdowndevelopments.blurb.network.ResponseModels.AutoCompleteResponse;
import com.mowdowndevelopments.blurb.network.Singletons;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MainViewModel extends AndroidViewModel {


    private MutableLiveData<List<AutoCompleteResponse>> autoCompleteDialogData;
    private MutableLiveData<LoadingStatus> logoutStatus;
    private MutableLiveData<String> errorMessage;
    private boolean loadingForDialog = false;

    public MainViewModel(@NonNull Application application) {
        super(application);
        errorMessage = new MutableLiveData<>();
        logoutStatus = new MutableLiveData<>(LoadingStatus.WAITING);
        autoCompleteDialogData = new MutableLiveData<>();
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<LoadingStatus> getLogoutStatus() {
        return logoutStatus;
    }

    public LiveData<List<AutoCompleteResponse>> getAutoCompleteDialogData() {
        return autoCompleteDialogData;
    }

    public void clearAutoCompleteDialogData(){
        autoCompleteDialogData.setValue(null);
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
                    Timber.e("onResponse: %s", errorMsg);
                }
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                logoutStatus.postValue(LoadingStatus.ERROR);
                Timber.e(t, "onFailure: %s", t.getMessage());
                errorMessage.postValue(t.getLocalizedMessage());
                FirebaseCrashlytics.getInstance().log(String.format("loginCallback.onFailure: %s", t.getMessage()));
                FirebaseCrashlytics.getInstance().recordException(t);
            }
        });
    }

    //Used for NewFeedDialogFragment;
    public void loadDataForFeedAutoComplete(String searchTerm){
        if (loadingForDialog) return;
        loadingForDialog = true;
        Singletons.getNewsBlurAPI(getApplication()).getAutoCompleteResults(searchTerm).enqueue(new Callback<List<AutoCompleteResponse>>() {
            //Will fail silently
            @Override
            public void onResponse(@NotNull Call<List<AutoCompleteResponse>> call, @NotNull Response<List<AutoCompleteResponse>> response) {
                loadingForDialog = false;
                if (response.isSuccessful()){
                    autoCompleteDialogData.postValue(response.body());
                }
            }

            @Override
            public void onFailure(@NotNull Call<List<AutoCompleteResponse>> call, @NotNull Throwable t) {
                loadingForDialog = false;
            }
        });
    }
}
