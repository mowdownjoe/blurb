package com.mowdowndevelopments.blurb.network;

import androidx.annotation.NonNull;

import com.mowdowndevelopments.blurb.network.ResponseModels.AuthResponse;
import com.mowdowndevelopments.blurb.network.ResponseModels.AutoCompleteResponse;
import com.mowdowndevelopments.blurb.network.ResponseModels.FeedContentsResponse;
import com.mowdowndevelopments.blurb.network.ResponseModels.GetFeedsResponse;

import java.util.List;
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
    Call<AuthResponse> login(@Field("username") @NonNull String username, @Field("password") @NonNull String password);

    @POST("/api/login")
    @FormUrlEncoded
    Call<AuthResponse> login(@Field("username") @NonNull String username);

    @POST("/api/logout")
    Call<Void> logout();

    @POST("/api/signup")
    @FormUrlEncoded
    Call<AuthResponse> signup(@Field("username") @NonNull String username,
                              @Field("password") @NonNull String password,
                              @Field("email") @NonNull String emailAddress);

    @POST("/api/signup")
    @FormUrlEncoded
    Call<AuthResponse> signup(@Field("username") String username, @Field("email") String emailAddress);

    @GET("/reader/feeds?flat=true")
    Call<GetFeedsResponse> getFeeds();

    @GET("/reader/feeds?flat=true&update_counts=true")
    Call<GetFeedsResponse> getFeedsAndRefreshCounts();

    @GET("/reader/feed/{id}?include_story_content=true")
    Call<FeedContentsResponse> getFeedContents(@Path("id") int feedId,
                                               @Query("read_filter") @NonNull String filter,
                                               @Query("order") @NonNull String sortOrder);

    @GET("/reader/river_stories?{concatenatedFeeds}")
    Call<FeedContentsResponse> getRiverOfNews(@Path("concatenatedFeeds") @NonNull String concatenatedFeedQueries);

    //TODO Create new ResponseModels for these Map<String, Object> calls

    @POST("/reader/add_url")
    @FormUrlEncoded
    Call<Map<String, Object>> addNewFeed(@Field("url") @NonNull String url);

    @POST("/reader/add_url")
    @FormUrlEncoded
    Call<Map<String, Object>> addNewFeed(@Field("url") @NonNull String url, @Field("folder") @NonNull String folderName);

    @POST("/reader/add_folder")
    @FormUrlEncoded
    Call<Map<String, Object>> createNewFolder(@Field("folder") @NonNull String folderName);

    @POST("/reader/add_folder")
    @FormUrlEncoded
    Call<Map<String, Object>> createNewFolder(@Field("folder") @NonNull String folderName,
                                              @Field("parent_folder") @NonNull String parentFolderName);

    @POST("/reader/mark_story_hash_as_unread")
    @FormUrlEncoded
    Call<Map<String, Object>> markStoryAsUnread(@Field("story_hash") @NonNull String storyHash);

    @POST("/reader/mark_story_hash_as_starred")
    @FormUrlEncoded
    Call<Map<String, Object>> markStoryAsStarred(@Field("story_hash") @NonNull String storyHash);

    @POST("/reader/mark_story_hash_as_unstarred")
    @FormUrlEncoded
    Call<Map<String, Object>> removeStarredStory(@Field("story_hash") @NonNull String storyHash);

    @GET("/rss_feeds/feed_autocomplete")
    Call<List<AutoCompleteResponse>> getAutoCompleteResults(@Query("term") @NonNull String searchTerm);
}
