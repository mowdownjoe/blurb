package com.mowdowndevelopments.blurb.network.ResponseModels;

import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.squareup.moshi.Json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    public ArrayList<Integer> getFeedIds(){
        ArrayList<Integer> ids = new ArrayList<>();
        for (String idString : feeds.keySet()) {
            ids.add(Integer.parseInt(idString));
        }
        return ids;
    }

    public HashMap<Integer, String> getInvertedFolderMap(){
        HashMap<Integer, String> invertedFolders = new HashMap<>();
        for (String key : folders.keySet()) {
            Integer[] values = Objects.requireNonNull(folders.get(key));
            for (Integer newKey : values) {
                invertedFolders.put(newKey, key);
            }
        }
        return invertedFolders;
    }
}
