package com.mowdowndevelopments.blurb.network;

import com.mowdowndevelopments.blurb.network.ResponseModels.AuthResponse;
import com.mowdowndevelopments.blurb.network.ResponseModels.FeedContentsResponse;
import com.mowdowndevelopments.blurb.network.ResponseModels.GetFeedsResponse;

import java.util.Map;

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
    Call<AuthResponse> login(@Field("username") String username, @Field("password") String password);

    @POST("/api/login")
    @FormUrlEncoded
    Call<AuthResponse> login(@Field("username") String username);

    @POST("/api/logout")
    Call<Void> logout();

    @POST("/api/signup")
    @FormUrlEncoded
    Call<AuthResponse> signup(@Field("username") String username,
                              @Field("password") String password,
                              @Field("email") String emailAddress);

    @POST("/api/signup")
    @FormUrlEncoded
    Call<AuthResponse> signup(@Field("username") String username, @Field("email") String emailAddress);

    @GET("/reader/feeds?flat=true")
    Call<GetFeedsResponse> getFeeds();

    @GET("/reader/feeds?flat=true&update_counts=true")
    Call<GetFeedsResponse> getFeedsAndRefreshCounts();

    @GET("/reader/feed/{id}")
    Call<FeedContentsResponse> getFeedContents(@Path("id") int feedId,
                                               @Query("read_filter") String filter,
                                               @Query("include_story_content") boolean includeStoryContent);

    @GET("/reader/river_stories?{concatenatedFeeds}")
    Call<FeedContentsResponse> getRiverOfNews(@Path("concatenatedFeeds") String concatenatedFeedQueries);

    //TODO Create new ResponseModels for these Map<String, Object> calls

    @POST("/reader/add_url")
    @FormUrlEncoded
    Call<Map<String, Object>> addNewFeed(@Field("url") String url);

    @POST("/reader/add_url")
    @FormUrlEncoded
    Call<Map<String, Object>> addNewFeed(@Field("url") String url, @Field("folder") String folderName);

    @POST("/reader/add_folder")
    @FormUrlEncoded
    Call<Map<String, Object>> createNewFolder(@Field("folder") String folderName);

    @POST("/reader/add_folder")
    @FormUrlEncoded
    Call<Map<String, Object>> createNewFolder(@Field("folder") String folderName, @Field("parent_folder") String parentFolderName);

    @POST("/reader/mark_story_hash_as_unread")
    @FormUrlEncoded
    Call<Map<String, Object>> markStoryAsUnread(@Field("story_hash") String storyHash);

    @POST("/reader/mark_story_hashes_as_read")
    @FormUrlEncoded
    Call<Map<String, Object>> markStoryAsRead(@Field("story_hash") String storyHash);

    @POST("/reader/mark_story_hash_as_starred")
    @FormUrlEncoded
    Call<Map<String, Object>> markStoryAsStarred(@Field("story_hash") String storyHash);

    @POST("/reader/mark_story_hash_as_unstarred")
    @FormUrlEncoded
    Call<Map<String, Object>> removeStarredStory(@Field("story_hash") String storyHash);
}
