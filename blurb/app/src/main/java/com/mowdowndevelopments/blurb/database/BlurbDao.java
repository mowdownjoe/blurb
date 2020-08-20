package com.mowdowndevelopments.blurb.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.database.entities.Story;

import java.util.Collection;
import java.util.List;

@Dao
public interface BlurbDao {

    @Insert
    void addFeeds(Collection<Feed> feeds);

    @Insert
    void addFeed(Feed feed);

    @Insert
    void addStories(Collection<Story> stories);

    @Insert
    void addStory(Story story);

    @Delete
    void removeStory(Story story);

    @Query("SELECT * FROM STORIES")
    LiveData<List<Story>> getFavoriteStories();

    @Query("select * from feeds")
    List<Feed> getFeeds();

    @Query("select feed_title from feeds where id = :id")
    String getFeedTitle(int id);

    @Query("select favicon_url from feeds where id = :id")
    String getFeedFaviconUrl(int id);
}
