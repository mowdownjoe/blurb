package com.mowdowndevelopments.blurb.network

import com.mowdowndevelopments.blurb.network.responseModels.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface NewsBlurAPI {
    @POST("/api/login")
    @FormUrlEncoded
    suspend fun login(@Field("username") username: String, @Field("password") password: String): Response<AuthResponse>

    @POST("/api/login")
    @FormUrlEncoded
    suspend fun login(@Field("username") username: String): Response<AuthResponse>

    @POST("/api/logout")
    suspend fun logout(): Response<Void>

    @POST("/api/signup")
    @FormUrlEncoded
    suspend fun signup(@Field("username") username: String,
               @Field("password") password: String,
               @Field("email") emailAddress: String): Response<AuthResponse>

    @POST("/api/signup")
    @FormUrlEncoded
    suspend fun signup(@Field("username") username: String, @Field("email") emailAddress: String): Response<AuthResponse>

    @GET("/reader/feeds?flat=true")
    fun getFeeds(): Call<GetFeedsResponse>

    @GET("/reader/feeds?flat=true&update_counts=true")
    fun getFeedsAndRefreshCounts(): Call<GetFeedsResponse>

    @GET("/reader/feed/{id}?include_story_content=true") //Required until Kotlin refactor complete
    fun getFeedContents(@Path("id") feedId: Int,
                        @Query("read_filter") filter: String,
                        @Query("order") sortOrder: String): Call<FeedContentsResponse>

    @GET("/reader/feed/{id}?include_story_content=true")
    fun getFeedContents(@Path("id") feedId: Int,
                        @Query("read_filter") filter: String,
                        @Query("order") sortOrder: String,
                        @Query("page") pageNumber: Int = 0): Call<FeedContentsResponse>

    @GET("/reader/river_stories?{concatenatedFeeds}")
    fun getRiverOfNews(@Path("concatenatedFeeds") concatenatedFeedQueries: String): Call<FeedContentsResponse>

    @POST("/reader/add_url")
    @FormUrlEncoded
    fun addNewFeed(@Field("url") url: String): Call<Map<String, Any>>

    @POST("/reader/add_url")
    @FormUrlEncoded
    fun addNewFeed(@Field("url") url: String, @Field("folder") folderName: String): Call<Map<String, Any>>

    @POST("/reader/add_folder")
    @FormUrlEncoded
    fun createNewFolder(@Field("folder") folderName: String): Call<Map<String, Any>>

    @POST("/reader/add_folder")
    @FormUrlEncoded
    fun createNewFolder(@Field("folder") folderName: String,
                        @Field("parent_folder") parentFolderName: String): Call<Map<String, Any>>

    @POST("/reader/mark_story_hash_as_unread")
    @FormUrlEncoded
    suspend fun markStoryAsUnread(@Field("story_hash") storyHash: String): Response<Map<String, Any>>

    @POST("/reader/mark_story_hash_as_starred")
    @FormUrlEncoded
    suspend fun markStoryAsStarred(@Field("story_hash") storyHash: String): Response<Map<String, Any>>

    @POST("/reader/mark_story_hash_as_unstarred")
    @FormUrlEncoded
    suspend fun removeStarredStory(@Field("story_hash") storyHash: String): Response<Map<String, Any>>

    @GET("/rss_feeds/feed_autocomplete")
    suspend fun getAutoCompleteResults(@Query("term") searchTerm: String): Response<List<AutoCompleteResponse>>

    @GET("/reader/starred_story_hashes")
    suspend fun getStarredStoryHashes(): Response<GetStarredHashesResponse>
}