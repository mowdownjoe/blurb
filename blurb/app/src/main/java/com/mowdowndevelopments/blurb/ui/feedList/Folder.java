package com.mowdowndevelopments.blurb.ui.feedList;

import com.mowdowndevelopments.blurb.database.entities.Feed;

import java.util.List;

public class Folder implements FeedListItem {
    private String name;
    private List<Feed> feeds;

    public Folder(String name, List<Feed> feeds) {
        this.name = name;
        this.feeds = feeds;
    }

    public String getName() {
        return name;
    }

    public List<Feed> getFeeds() {
        return feeds;
    }
}
