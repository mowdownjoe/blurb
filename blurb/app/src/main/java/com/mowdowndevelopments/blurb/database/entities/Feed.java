package com.mowdowndevelopments.blurb.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.squareup.moshi.Json;

import java.io.Serializable;

@Entity(tableName = "feeds")
public class Feed implements Serializable {

    public static final String ID = "id";
    public static final String TITLE = "feed_title";
    public static final String ADDRESS = "feed_address";
    public static final String LINK = "feed_link";
    public static final String FAVICON = "favicon_url";

    @PrimaryKey(autoGenerate = false)
    private int id;
    @Json(name = TITLE)
    @ColumnInfo(name = TITLE)
    private String feedTitle;
    @Json(name = ADDRESS)
    @ColumnInfo(name = ADDRESS)
    private String feedAddress;
    @Json(name = LINK)
    @ColumnInfo(name = LINK)
    private String feedLink;
    @Json(name = FAVICON)
    @ColumnInfo(name = FAVICON)
    private String favIconUrl;

    public Feed(int id, String feedTitle, String feedAddress, String feedLink, String favIconUrl) {
        this.id = id;
        this.feedTitle = feedTitle;
        this.feedAddress = feedAddress;
        this.feedLink = feedLink;
        this.favIconUrl = favIconUrl;
    }

    public int getId() {
        return id;
    }

    public String getFeedTitle() {
        return feedTitle;
    }

    public String getFeedAddress() {
        return feedAddress;
    }

    public String getFeedLink() {
        return feedLink;
    }

    public String getFavIconUrl() {
        return favIconUrl;
    }
}
