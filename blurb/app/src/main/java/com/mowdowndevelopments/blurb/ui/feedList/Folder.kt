package com.mowdowndevelopments.blurb.ui.feedList

import com.mowdowndevelopments.blurb.database.entities.Feed

data class Folder(val name: String, val feeds: List<Feed>) : FeedListItem