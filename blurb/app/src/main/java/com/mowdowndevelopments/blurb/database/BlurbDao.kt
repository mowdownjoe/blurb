package com.mowdowndevelopments.blurb.database

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.mowdowndevelopments.blurb.database.entities.Feed
import com.mowdowndevelopments.blurb.database.entities.Story

@Dao
interface BlurbDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFeeds(feeds: Collection<Feed>)

    @Insert
    fun addFeed(feed: Feed)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addStories(stories: Collection<Story>)

    @Insert
    suspend fun addStory(story: Story)

    @Delete
    suspend fun removeStory(story: Story)

    @Query("SELECT * FROM STORIES ORDER BY story_timestamp DESC")
    fun getStarredStoryPagingSourceFactory(): DataSource.Factory<Int, Story>

    @Query("SELECT * FROM STORIES ORDER BY story_timestamp DESC")
    fun getStarredStoryList(): LiveData<List<Story>>

    @Query("SELECT * FROM STORIES ORDER BY story_timestamp DESC LIMIT 20")
    fun getStarredStoryListForWidget(): List<Story>

    @Query("select * from feeds")
    fun getFeeds(): List<Feed>

    @Query("select feed_title from feeds where id = :id")
    fun getFeedTitle(id: Int): String

    @Query("select favicon_url from feeds where id = :id")
    fun getFeedFaviconUrl(id: Int): String

    @Query("select exists(select 1 from stories where story_hash = :storyHash)")
    suspend fun doesStoryExist(storyHash: String?): LiveData<Boolean>
}