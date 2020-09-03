package com.mowdowndevelopments.blurb.database.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.squareup.moshi.Json;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Keep
@Entity(tableName = Story.TABLE_NAME,
        foreignKeys = @ForeignKey(entity = Feed.class,
                onDelete = ForeignKey.SET_NULL,
                childColumns = Story.ID,
                parentColumns = Feed.ID))
public class Story implements Parcelable {

    static final String TABLE_NAME = "stories";
    private static final String HASH = "story_hash";
    private static final String TITLE = "story_title";
    private static final String CONTENT = "story_content";
    private static final String TIMESTAMP = "story_timestamp";
    private static final String AUTHORS = "story_authors";
    private static final String PERMALINK = "story_permalink";
    static final String ID = "story_feed_id";

    @PrimaryKey
    @NonNull
    @Json(name = HASH)
    @ColumnInfo(name = HASH)
    private String storyHash;
    @Json(name = TITLE)
    @ColumnInfo(name = TITLE)
    private String title;
    @Json(name = CONTENT)
    @ColumnInfo(name = CONTENT)
    private String content;
    @Json(name = TIMESTAMP)
    @ColumnInfo(name = TIMESTAMP)
    private String timestampString;
    @Json(name = AUTHORS)
    @ColumnInfo(name = AUTHORS)
    private String authors;
    @Json(name = PERMALINK)
    @ColumnInfo(name = PERMALINK)
    private String permalink;
    @Json(name = ID)
    @ColumnInfo(name = ID, index = true)
    private int feedId;
    @Ignore
    @Json(name = "read_status")
    private int readStatus;

    public Story(@NotNull String storyHash, String content, String title, String timestampString, String authors, String permalink, int feedId) {
        this.storyHash = storyHash;
        this.content = content;
        this.title = title;
        this.timestampString = timestampString;
        this.authors = authors;
        this.permalink = permalink;
        this.feedId = feedId;
        readStatus = 1;
    }

    @Ignore
    public Story(@NotNull String storyHash, String content, String title, String timestampString, String authors, String permalink) {
        this.storyHash = storyHash;
        this.title = title;
        this.content = content;
        this.timestampString = timestampString;
        this.authors = authors;
        this.permalink = permalink;
        feedId = -1;
        readStatus = 1;
    }

    @Ignore
    public Story(Story orphanedStory, int feedId){
        storyHash = orphanedStory.storyHash;
        title = orphanedStory.title;
        content = orphanedStory.content;
        timestampString = orphanedStory.timestampString;
        authors = orphanedStory.authors;
        permalink = orphanedStory.permalink;
        readStatus = orphanedStory.readStatus;
        this.feedId = feedId;
    }

    @Ignore
    public Story(@NotNull String storyHash, String title, String content, String timestampString, String authors, String permalink, int feedId, int readStatus) {
        this.storyHash = storyHash;
        this.title = title;
        this.content = content;
        this.timestampString = timestampString;
        this.authors = authors;
        this.permalink = permalink;
        this.feedId = feedId;
        this.readStatus = readStatus;
    }

    @Ignore
    protected Story(Parcel in) {
        storyHash = Objects.requireNonNull(in.readString());
        content = in.readString();
        title = in.readString();
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
        parcel.writeString(title);
        parcel.writeString(timestampString);
        parcel.writeString(authors);
        parcel.writeString(permalink);
        parcel.writeInt(feedId);
    }

    @NotNull
    public String getStoryHash() {
        return storyHash;
    }

    public String getContent() {
        return content;
    }

    public String getTitle() {
        return title;
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

    public int getReadStatus() {
        return readStatus;
    }

    public boolean isRead() { return readStatus == 1; }

    public void setIsRead(boolean readStatus) {
        if (readStatus){
            this.readStatus = 1;
        } else {
            this.readStatus = 0;
        }
    }

    public long getTimestamp(){
        return Long.parseLong(timestampString);
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
