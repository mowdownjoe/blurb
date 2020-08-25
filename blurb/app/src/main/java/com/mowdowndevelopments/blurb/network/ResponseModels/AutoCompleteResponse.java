package com.mowdowndevelopments.blurb.network.ResponseModels;

import com.squareup.moshi.Json;

public class AutoCompleteResponse {
    @Json(name = "label")
    private String feedTitle;
    private String tagline;
    @Json(name = "value")
    private String url;

    public AutoCompleteResponse(String feedTitle, String tagline, String url) {
        this.feedTitle = feedTitle;
        this.tagline = tagline;
        this.url = url;
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
