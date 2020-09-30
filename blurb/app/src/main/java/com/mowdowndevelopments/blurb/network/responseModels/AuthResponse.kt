package com.mowdowndevelopments.blurb.network.responseModels

import com.squareup.moshi.Json

data class AuthResponse(val code: Int, val isAuthenticated: Boolean, @field:Json(name = "user_id") val userId: Int?)