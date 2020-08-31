package com.mowdowndevelopments.blurb.ui.feeds.single;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;

import com.mowdowndevelopments.blurb.database.entities.Story;

public class SingleFeedDataSourceFactory extends DataSource.Factory<Integer, Story> {

    private Context context;
    private int feedId;
    private String sortOrder;
    private String filter;

    public SingleFeedDataSourceFactory(Context context, int feedId, String sortOrder, String filter) {
        this.context = context;
        this.feedId = feedId;
        this.sortOrder = sortOrder;
        this.filter = filter;
    }

    @NonNull
    @Override
    public SingleFeedDataSource create() {
        return new SingleFeedDataSource(context, feedId, sortOrder, filter);
    }
}
