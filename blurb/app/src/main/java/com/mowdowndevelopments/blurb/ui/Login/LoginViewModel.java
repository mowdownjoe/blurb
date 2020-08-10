package com.mowdowndevelopments.blurb.ui.Login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.mowdowndevelopments.blurb.network.Singletons;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {

    private final Callback<Void> loginCallback;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        loginCallback = new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                //TODO
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        };
    }

    // TODO: Implement the ViewModel
    public void login(String username, String password){
        Singletons.getNewsBlurAPI(getApplication()).login(username, password).enqueue(loginCallback);
    }

    public void login(String username){
        Singletons.getNewsBlurAPI(getApplication()).login(username).enqueue(loginCallback);
    }
}