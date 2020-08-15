package com.mowdowndevelopments.blurb.network;

import com.mowdowndevelopments.blurb.network.ResponseModels.AuthResponse;
import com.mowdowndevelopments.blurb.network.ResponseModels.FeedContentsResponse;
import com.mowdowndevelopments.blurb.network.ResponseModels.GetFeedsResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NewsBlurAPI {
    //TODO Fill out with calls

    @POST("/api/login")
    @FormUrlEncoded
    public Call<AuthResponse> login(@Field("username") String username, @Field("password") String password);

    @POST("/api/login")
    @FormUrlEncoded
    public Call<AuthResponse> login(@Field("username") String username);

    @POST("/api/logout")
    public Call<Void> logout();

    @POST("/api/signup")
    @FormUrlEncoded
    public Call<AuthResponse> signup(@Field("username") String username,
                             @Field("password") String password,
                             @Field("email") String emailAddress);

    @POST("/api/signup")
    @FormUrlEncoded
    public Call<AuthResponse> signup(@Field("username") String username, @Field("email") String emailAddress);

    @GET("/reader/feeds?flat=true")
    public Call<GetFeedsResponse> getFeeds();

    @GET("/reader/feeds?flat=true&update_counts=true")
    public Call<GetFeedsResponse> getFeedsAndRefreshCounts();

    @GET("/reader/feed/{id}")
    public Call<FeedContentsResponse> getFeedContents(@Path("id") int feedId,
                                                      @Query("read_filter") String filter,
                                                      @Query("include_story_content") boolean includeStoryContent);

    @GET("/reader/river_stories?{concatenatedFeeds}")
    public Call<FeedContentsResponse> getRiverOfNews(@Path("concatenatedFeeds") String concatenatedFeedQueries);
}
