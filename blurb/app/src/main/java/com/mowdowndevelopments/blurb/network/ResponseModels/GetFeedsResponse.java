package com.mowdowndevelopments.blurb.network.ResponseModels;

import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.squareup.moshi.Json;

import java.util.Map;

public class GetFeedsResponse {
    @Json(name = "flat_folders")
    private Map<String, Integer[]> folders;
    private Map<String, Feed> feeds;

    public GetFeedsResponse(Map<String, Integer[]> folders, Map<String, Feed> feeds) {
        this.folders = folders;
        this.feeds = feeds;
    }

    public Map<String, Integer[]> getFolders() {
        return folders;
    }

    public Map<String, Feed> getFeeds() {
        return feeds;
    }
}
