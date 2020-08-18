package com.mowdowndevelopments.blurb.ui.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.network.LoadingStatus;
import com.mowdowndevelopments.blurb.network.ResponseModels.AuthResponse;
import com.mowdowndevelopments.blurb.network.Singletons;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class LoginViewModel extends AndroidViewModel {

    private MutableLiveData<LoadingStatus> loginStatus;
    private MutableLiveData<String> errorToast;

    private final Callback<AuthResponse> loginCallback= new Callback<AuthResponse>() {
        @Override
        public void onResponse(@NotNull Call<AuthResponse> call, @NotNull Response<AuthResponse> response) {
            if (response.isSuccessful()) {
                if (response.body() != null && response.body().isAuthenticated()) {
                    loginStatus.postValue(LoadingStatus.DONE);
                } else {
                    loginStatus.postValue(LoadingStatus.ERROR);
                    errorToast.postValue(getApplication().getString(R.string.bad_credential_error));
                    Timber.w("loginCallback.onResponse: User entered incorrect credentials.");
                }
            } else {
                loginStatus.postValue(LoadingStatus.ERROR);
                //TODO Add messages to explain HTTP Error codes in plain language
                String errorMsg = getApplication().getString(R.string.http_error, response.code());
                errorToast.postValue(errorMsg);
                Timber.e("loginCallback.onResponse: %s", errorMsg);
            }
        }

        @Override
        public void onFailure(@NotNull Call<AuthResponse> call, @NotNull Throwable t) {
            loginStatus.postValue(LoadingStatus.ERROR);
            Timber.e(t, "loginCallback.onFailure: %s", t.getMessage());
            errorToast.postValue(t.getLocalizedMessage());
        }
    };
    private final Callback<AuthResponse> registrationCallback= new Callback<AuthResponse>() {
        @Override
        public void onResponse(@NotNull Call<AuthResponse> call, @NotNull Response<AuthResponse> response) {
            if (response.isSuccessful()) {
                if (response.body() != null && response.body().isAuthenticated()) {
                    loginStatus.postValue(LoadingStatus.DONE);
                } else {
                    loginStatus.postValue(LoadingStatus.ERROR);
                    errorToast.postValue(getApplication().getString(R.string.bad_registration_error));
                    Timber.w("registrationCallback.onResponse: User entered incorrect credentials.");
                }
            } else {
                loginStatus.postValue(LoadingStatus.ERROR);
                //TODO Add messages to explain HTTP Error codes in plain language
                String errorMsg = getApplication().getString(R.string.http_error, response.code());
                errorToast.postValue(errorMsg);
                Timber.e("loginCallback.onResponse: %s", errorMsg);
            }
        }

        @Override
        public void onFailure(@NotNull Call<AuthResponse> call, @NotNull Throwable t) {
            loginStatus.postValue(LoadingStatus.ERROR);
            Timber.e(t, "loginCallback.onFailure: %s", t.getMessage());
            errorToast.postValue(t.getLocalizedMessage());
        }
    };

    public LiveData<LoadingStatus> getLoginStatus(){ return loginStatus; }

    public LiveData<String> getErrorToast() {
        return errorToast;
    }

    public LoginViewModel(@NonNull Application application) {
        super(application);
        loginStatus = new MutableLiveData<>(LoadingStatus.WAITING);
        errorToast = new MutableLiveData<>();
    }

    public void login(String username, String password){
        if (loginStatus.getValue() == LoadingStatus.LOADING) return;
        loginStatus.postValue(LoadingStatus.LOADING);
        Singletons.getNewsBlurAPI(getApplication()).login(username, password).enqueue(loginCallback);
    }

    public void login(String username){
        if (loginStatus.getValue() == LoadingStatus.LOADING) return;
        loginStatus.postValue(LoadingStatus.LOADING);
        Singletons.getNewsBlurAPI(getApplication()).login(username).enqueue(loginCallback);
    }

    public void registerNewAccount(String username, String password, String emailAddress){
        if (loginStatus.getValue() == LoadingStatus.LOADING) return;
        loginStatus.postValue(LoadingStatus.LOADING);
        Singletons.getNewsBlurAPI(getApplication()).signup(username, password, emailAddress).enqueue(registrationCallback);
    }

    public void registerNewAccount(String username, String emailAddress){
        if (loginStatus.getValue() == LoadingStatus.LOADING) return;
        loginStatus.postValue(LoadingStatus.LOADING);
        Singletons.getNewsBlurAPI(getApplication()).signup(username, emailAddress).enqueue(registrationCallback);
    }
}