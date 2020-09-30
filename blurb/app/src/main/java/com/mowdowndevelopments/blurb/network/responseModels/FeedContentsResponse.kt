package com.mowdowndevelopments.blurb.network.ResponseModels;

import com.mowdowndevelopments.blurb.database.entities.Story;

public class FeedContentsResponse {
    private Story[] stories;

    public FeedContentsResponse(Story[] stories) {
        this.stories = stories;
    }

    public Story[] getStories() {
        return stories;
    }
}
