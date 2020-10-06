package com.mowdowndevelopments.blurb.database.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import java.util.*
import java.util.Objects.requireNonNull

@Keep
@Entity(tableName = Story.TABLE_NAME)
class Story : Parcelable {
    @PrimaryKey
    @Json(name = HASH)
    @ColumnInfo(name = HASH, index = true)
    var storyHash: String
        private set

    @Json(name = TITLE)
    @ColumnInfo(name = TITLE)
    var title: String?
        private set

    @Json(name = CONTENT)
    @ColumnInfo(name = CONTENT)
    var content: String?
        private set

    @Json(name = TIMESTAMP)
    @ColumnInfo(name = TIMESTAMP)
    var timestampString: String?
        private set

    @Json(name = AUTHORS)
    @ColumnInfo(name = AUTHORS)
    var authors: String?
        private set

    @Json(name = PERMALINK)
    @ColumnInfo(name = PERMALINK)
    var permalink: String?
        private set

    @Json(name = ID)
    @ColumnInfo(name = ID)
    var feedId: Int
        private set

    @Ignore
    @Json(name = "read_status")
    var readStatus = 0
        private set

    constructor(storyHash: String, content: String?, title: String?, timestampString: String?, authors: String?, permalink: String?, feedId: Int) {
        this.storyHash = storyHash
        this.content = content
        this.title = title
        this.timestampString = timestampString
        this.authors = authors
        this.permalink = permalink
        this.feedId = feedId
        readStatus = 1
    }

    @Ignore
    constructor(storyHash: String, content: String?, title: String?, timestampString: String?, authors: String?, permalink: String?) {
        this.storyHash = storyHash
        this.title = title
        this.content = content
        this.timestampString = timestampString
        this.authors = authors
        this.permalink = permalink
        feedId = -1
        readStatus = 1
    }

    @Ignore
    constructor(orphanedStory: Story, feedId: Int) {
        storyHash = orphanedStory.storyHash
        title = orphanedStory.title
        content = orphanedStory.content
        timestampString = orphanedStory.timestampString
        authors = orphanedStory.authors
        permalink = orphanedStory.permalink
        readStatus = orphanedStory.readStatus
        this.feedId = feedId
    }

    @Ignore
    constructor(storyHash: String, title: String?, content: String?, timestampString: String?, authors: String?, permalink: String?, feedId: Int, readStatus: Int) {
        this.storyHash = storyHash
        this.title = title
        this.content = content
        this.timestampString = timestampString
        this.authors = authors
        this.permalink = permalink
        this.feedId = feedId
        this.readStatus = readStatus
    }

    @Ignore
    protected constructor(`in`: Parcel) {
        storyHash = requireNonNull(`in`.readString()).toString()
        content = `in`.readString()
        title = `in`.readString()
        timestampString = `in`.readString()
        authors = `in`.readString()
        permalink = `in`.readString()
        feedId = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(storyHash)
        parcel.writeString(content)
        parcel.writeString(title)
        parcel.writeString(timestampString)
        parcel.writeString(authors)
        parcel.writeString(permalink)
        parcel.writeInt(feedId)
    }

    var isRead: Boolean
        get() = readStatus == 1
        set(readStatus) {
            if (readStatus) {
                this.readStatus = 1
            } else {
                this.readStatus = 0
            }
        }
    val timestamp: Long
        get() = timestampString?.toLong() ?: -1

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val story = other as Story
        return feedId == story.feedId && storyHash == story.storyHash && timestampString == story.timestampString && permalink == story.permalink
    }

    override fun hashCode(): Int {
        return Objects.hash(storyHash, timestampString, permalink, feedId)
    }

    companion object {
        const val TABLE_NAME = "stories"
        private const val HASH = "story_hash"
        private const val TITLE = "story_title"
        private const val CONTENT = "story_content"
        private const val TIMESTAMP = "story_timestamp"
        private const val AUTHORS = "story_authors"
        private const val PERMALINK = "story_permalink"
        const val ID = "story_feed_id"
        val CREATOR: Parcelable.Creator<Story?> = object : Parcelable.Creator<Story?> {
            override fun createFromParcel(`in`: Parcel): Story? {
                return Story(`in`)
            }

            override fun newArray(size: Int): Array<Story?> {
                return arrayOfNulls(size)
            }
        }
    }
}