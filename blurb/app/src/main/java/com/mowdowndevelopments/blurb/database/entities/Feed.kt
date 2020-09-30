package com.mowdowndevelopments.blurb.database.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.mowdowndevelopments.blurb.ui.feedList.FeedListItem
import com.squareup.moshi.Json
import java.util.*

@Keep
@Entity(tableName = Feed.TABLE_NAME)
class Feed : Parcelable, FeedListItem {
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(index = true, name = ID)
    var id: Int
        private set

    @Json(name = TITLE)
    @ColumnInfo(name = TITLE)
    var feedTitle: String?
        private set

    @Json(name = ADDRESS)
    @ColumnInfo(name = ADDRESS)
    var feedAddress: String?
        private set

    @Json(name = LINK)
    @ColumnInfo(name = LINK)
    var feedLink: String?
        private set

    @Json(name = FAVICON)
    @ColumnInfo(name = FAVICON)
    var favIconUrl: String?
        private set

    @Ignore
    @Json(name = "nt")
    var unreadCount: Int
        private set

    @Ignore
    @Json(name = "ps")
    var preferredUnreadCount: Int
        private set

    constructor(id: Int, feedTitle: String?, feedAddress: String?, feedLink: String?, favIconUrl: String?) {
        this.id = id
        this.feedTitle = feedTitle
        this.feedAddress = feedAddress
        this.feedLink = feedLink
        this.favIconUrl = favIconUrl
        unreadCount = -1
        preferredUnreadCount = -1
    }

    @Ignore
    constructor(id: Int, feedTitle: String?, feedAddress: String?, feedLink: String?, favIconUrl: String?, unreadCount: Int, preferredUnreadCount: Int) {
        this.id = id
        this.feedTitle = feedTitle
        this.feedAddress = feedAddress
        this.feedLink = feedLink
        this.favIconUrl = favIconUrl
        this.unreadCount = unreadCount
        this.preferredUnreadCount = preferredUnreadCount
    }

    private constructor(`in`: Parcel) {
        id = `in`.readInt()
        feedTitle = `in`.readString()
        feedAddress = `in`.readString()
        feedLink = `in`.readString()
        favIconUrl = `in`.readString()
        unreadCount = -1
        preferredUnreadCount = -1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val feed = other as Feed
        return id == feed.id && feedTitle == feed.feedTitle && feedLink == feed.feedLink && favIconUrl == feed.favIconUrl
    }

    override fun hashCode(): Int {
        return Objects.hash(id, feedTitle, feedLink, favIconUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(feedTitle)
        parcel.writeString(feedAddress)
        parcel.writeString(feedLink)
        parcel.writeString(favIconUrl)
    }

    companion object {
        const val ID = "id"
        private const val TITLE = "feed_title"
        private const val ADDRESS = "feed_address"
        private const val LINK = "feed_link"
        private const val FAVICON = "favicon_url"
        const val TABLE_NAME = "feeds"
        @JvmField
        val CREATOR: Parcelable.Creator<Feed?> = object : Parcelable.Creator<Feed?> {
            override fun createFromParcel(parcel: Parcel): Feed? {
                return Feed(parcel)
            }

            override fun newArray(size: Int): Array<Feed?> {
                return arrayOfNulls(size)
            }
        }
    }
}