package com.mowdowndevelopments.blurb.network.ResponseModels;

import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

public class AuthResponse {
    private int code;
    private boolean authenticated;
    @Nullable
    @Json(name = "user_id")
    private Integer userId;

    public AuthResponse(int code, boolean authenticated, @Nullable Integer userId) {
        this.code = code;
        this.authenticated = authenticated;
        this.userId = userId;
    }

    public int getCode() {
        return code;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    @Nullable
    public Integer getUserId() {
        return userId;
    }
}
