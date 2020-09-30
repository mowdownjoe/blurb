package com.mowdowndevelopments.blurb.network.ResponseModels;

import com.squareup.moshi.Json;

import java.util.List;

public class GetStarredHashesResponse {

    @Json(name = "starred_story_hashes")
    private List<String> starredStoryHashes;

    public GetStarredHashesResponse(List<String> starredStoryHashes) {
        this.starredStoryHashes = starredStoryHashes;
    }

    public List<String> getStarredStoryHashes() {
        return starredStoryHashes;
    }
}
