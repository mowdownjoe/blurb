package com.mowdowndevelopments.blurb.network;

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
    public Call<Void> login(@Field("username") String username, @Field("password") String password);

    @POST("/api/login")
    @FormUrlEncoded
    public Call<Void> login(@Field("username") String username);

    @POST("/api/logout")
    public Call<Void> logout();

    @GET("/reader/feeds?flat=true")
    public Call<GetFeedsResponse> getFeeds();

    @GET("/reader/feed/{id}")
    public Call<FeedContentsResponse> getFeedContents(@Path("id") int feedId,
                                                      @Query("read_filter") String filter,
                                                      @Query("include_story_content") boolean includeStoryContent);
}
