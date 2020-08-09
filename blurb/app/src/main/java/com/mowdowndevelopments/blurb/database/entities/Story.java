package com.mowdowndevelopments.blurb.database.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import java.util.Objects;

@Keep
@Entity(tableName = Story.TABLE_NAME,
        foreignKeys = @ForeignKey(entity = Feed.class,
                childColumns = Feed.ID,
                onDelete = ForeignKey.SET_NULL,
                parentColumns = Story.FEED_ID))
public class Story implements Parcelable {
    public static final String FEED_ID = "feed_id";
    public static final String TABLE_NAME = "stories";


    private String storyHash;
    private String content;
    private String timestampString;
    private String authors;
    private String permalink;
    private int feedId;

    public Story(String storyHash, String content, String timestampString, String authors, String permalink, int feedId) {
        this.storyHash = storyHash;
        this.content = content;
        this.timestampString = timestampString;
        this.authors = authors;
        this.permalink = permalink;
        this.feedId = feedId;
    }

    protected Story(Parcel in) {
        storyHash = in.readString();
        content = in.readString();
        timestampString = in.readString();
        authors = in.readString();
        permalink = in.readString();
        feedId = in.readInt();
    }

    public static final Creator<Story> CREATOR = new Creator<Story>() {
        @Override
        public Story createFromParcel(Parcel in) {
            return new Story(in);
        }

        @Override
        public Story[] newArray(int size) {
            return new Story[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(storyHash);
        parcel.writeString(content);
        parcel.writeString(timestampString);
        parcel.writeString(authors);
        parcel.writeString(permalink);
        parcel.writeInt(feedId);
    }

    public long getTimestamp(){
        return Long.parseLong(timestampString);
    }

    public String getStoryHash() {
        return storyHash;
    }

    public String getContent() {
        return content;
    }

    public String getTimestampString() {
        return timestampString;
    }

    public String getAuthors() {
        return authors;
    }

    public String getPermalink() {
        return permalink;
    }

    public int getFeedId() {
        return feedId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Story story = (Story) o;
        return feedId == story.feedId &&
                storyHash.equals(story.storyHash) &&
                timestampString.equals(story.timestampString) &&
                permalink.equals(story.permalink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storyHash, timestampString, permalink, feedId);
    }
}
