package com.mowdowndevelopments.blurb.database.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.mowdowndevelopments.blurb.ui.feedList.FeedListItem;
import com.squareup.moshi.Json;

import java.util.Objects;

@Keep
@Entity(tableName = Feed.TABLE_NAME)
public class Feed implements Parcelable, FeedListItem {

    static final String ID = "id";
    private static final String TITLE = "feed_title";
    private static final String ADDRESS = "feed_address";
    private static final String LINK = "feed_link";
    private static final String FAVICON = "favicon_url";
    static final String TABLE_NAME = "feeds";

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(index = true)
    private int id;
    @Json(name = TITLE)
    @ColumnInfo(name = TITLE)
    private String feedTitle;
    @Json(name = ADDRESS)
    @ColumnInfo(name = ADDRESS)
    private String feedAddress;
    @Json(name = LINK)
    @ColumnInfo(name = LINK)
    private String feedLink;
    @Json(name = FAVICON)
    @ColumnInfo(name = FAVICON)
    private String favIconUrl;
    @Ignore
    @Json(name = "nt")
    private int unreadCount;
    @Ignore
    @Json(name = "ps")
    private int preferredUnreadCount;

    public Feed(int id, String feedTitle, String feedAddress, String feedLink, String favIconUrl) {
        this.id = id;
        this.feedTitle = feedTitle;
        this.feedAddress = feedAddress;
        this.feedLink = feedLink;
        this.favIconUrl = favIconUrl;
        unreadCount = -1;
        preferredUnreadCount = -1;
    }

    @Ignore
    public Feed(int id, String feedTitle, String feedAddress, String feedLink, String favIconUrl, int unreadCount, int preferredUnreadCount) {
        this.id = id;
        this.feedTitle = feedTitle;
        this.feedAddress = feedAddress;
        this.feedLink = feedLink;
        this.favIconUrl = favIconUrl;
        this.unreadCount = unreadCount;
        this.preferredUnreadCount = preferredUnreadCount;
    }

    private Feed(Parcel in){
        id = in.readInt();
        feedTitle = in.readString();
        feedAddress = in.readString();
        feedLink = in.readString();
        favIconUrl = in.readString();
        unreadCount = -1;
        preferredUnreadCount = -1;
    }

    public static final Parcelable.Creator<Feed> CREATOR = new Parcelable.Creator<Feed>() {

        @Override
        public Feed createFromParcel(Parcel parcel) {
            return new Feed(parcel);
        }

        @Override
        public Feed[] newArray(int size) {
            return new Feed[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getFeedTitle() {
        return feedTitle;
    }

    public String getFeedAddress() {
        return feedAddress;
    }

    public String getFeedLink() {
        return feedLink;
    }

    public String getFavIconUrl() {
        return favIconUrl;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public int getPreferredUnreadCount() {
        return preferredUnreadCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feed feed = (Feed) o;
        return id == feed.id &&
                feedTitle.equals(feed.feedTitle) &&
                feedLink.equals(feed.feedLink) &&
                favIconUrl.equals(feed.favIconUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, feedTitle, feedLink, favIconUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(feedTitle);
        parcel.writeString(feedAddress);
        parcel.writeString(feedLink);
        parcel.writeString(favIconUrl);
    }
}
