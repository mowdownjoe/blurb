package com.mowdowndevelopments.blurb.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;

import java.io.Serializable;

@Entity(tableName = "stories",
        foreignKeys = @ForeignKey(entity = Feed.class,
                childColumns = Feed.ID,
                onDelete = ForeignKey.SET_NULL,
                parentColumns = Story.FEED_ID))
public class Story implements Serializable {
    public static final String FEED_ID = "feed_id";


}
