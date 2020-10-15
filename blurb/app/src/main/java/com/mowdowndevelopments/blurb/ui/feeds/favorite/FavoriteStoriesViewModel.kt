package com.mowdowndevelopments.blurb.ui.feeds.favorite

import android.app.Application
import androidx.lifecycle.LiveData
import com.mowdowndevelopments.blurb.database.BlurbDb.Companion.getInstance
import com.mowdowndevelopments.blurb.database.entities.Story
import com.mowdowndevelopments.blurb.ui.feeds.BaseFeedViewModel

class FavoriteStoriesViewModel(app: Application) : BaseFeedViewModel(app) {
    @JvmField
    val storyList: LiveData<List<Story>> = getInstance(app).blurbDao().getStarredStoryList()

}