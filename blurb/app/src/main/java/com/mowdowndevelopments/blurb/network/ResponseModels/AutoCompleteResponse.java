package com.mowdowndevelopments.blurb.network.ResponseModels;

import com.squareup.moshi.Json;

public class AutoCompleteResponse {
    @Json(name = "label")
    private String feedTitle;
    @Json(name = "num_subscribers")
    private int subscriberCount;
    private String tagline;
    @Json(name = "value")
    private String url;

    public AutoCompleteResponse(String feedTitle, int subscriberCount, String tagline, String url) {
        this.feedTitle = feedTitle;
        this.subscriberCount = subscriberCount;
        this.tagline = tagline;
        this.url = url;
    }

    public int getSubscriberCount() {
        return subscriberCount;
    }

    public String getFeedTitle() {
        return feedTitle;
    }

    public String getTagline() {
        return tagline;
    }

    public String getUrl() {
        return url;
    }

}
