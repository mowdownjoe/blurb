package com.mowdowndevelopments.blurb.network.ResponseModels;

import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.squareup.moshi.Json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GetFeedsResponse {
    /*
    * Maps name of folder to feed IDs.
    * */
    @Json(name = "flat_folders")
    private Map<String, Integer[]> folders;
    /*
    * Maps feed ID parsed to String to its appropriate feed.
    * */
    private Map<String, Feed> feeds; //Key will be Feed ID parsed to String

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
            for (Integer newKey : Objects.requireNonNull(folders.get(key))) {
                invertedFolders.put(newKey, key);
            }
        }
        return invertedFolders;
    }
}
