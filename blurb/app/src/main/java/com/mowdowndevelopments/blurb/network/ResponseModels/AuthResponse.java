package com.mowdowndevelopments.blurb.network.ResponseModels;

import com.squareup.moshi.Json;

public class AuthResponse {
    private int code;
    private boolean authenticated;
    @Json(name = "user_id")
    private Integer userId;

    public AuthResponse(int code, boolean authenticated, Integer userId) {
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

    public Integer getUserId() {
        return userId;
    }
}
