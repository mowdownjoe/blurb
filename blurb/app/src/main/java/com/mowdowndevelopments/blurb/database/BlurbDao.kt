package com.mowdowndevelopments.blurb.database;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.database.entities.Story;

import java.util.Collection;
import java.util.List;

@Dao
public interface BlurbDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addFeeds(Collection<Feed> feeds);

    @Insert
    void addFeed(Feed feed);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addStories(Collection<Story> stories);

    @Insert
    void addStory(Story story);

    @Delete
    void removeStory(Story story);

    @Query("SELECT * FROM STORIES ORDER BY story_timestamp DESC")
    DataSource.Factory<Integer, Story> getStarredStoryPagingSourceFactory();

    @Query("SELECT * FROM STORIES ORDER BY story_timestamp DESC")
    LiveData<List<Story>> getStarredStoryList();

    @Query("SELECT * FROM STORIES ORDER BY story_timestamp DESC LIMIT 20")
    List<Story> getStarredStoryListForWidget();

    @Query("select * from feeds")
    List<Feed> getFeeds();

    @Query("select feed_title from feeds where id = :id")
    String getFeedTitle(int id);

    @Query("select favicon_url from feeds where id = :id")
    String getFeedFaviconUrl(int id);

    @Query("select exists(select 1 from stories where story_hash = :storyHash)")
    LiveData<Boolean> doesStoryExist(String storyHash);
}
