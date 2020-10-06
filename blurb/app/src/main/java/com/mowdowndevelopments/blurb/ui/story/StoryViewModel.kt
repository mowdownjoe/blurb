package com.mowdowndevelopments.blurb.ui.story

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mowdowndevelopments.blurb.AppExecutors
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.database.BlurbDb
import com.mowdowndevelopments.blurb.database.entities.Story
import com.mowdowndevelopments.blurb.network.Singletons
import com.mowdowndevelopments.blurb.network.Singletons.getNewsBlurAPI
import com.mowdowndevelopments.blurb.network.Singletons.getOkHttpClient
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.gildor.coroutines.okhttp.await
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

class StoryViewModel(app: Application?) : AndroidViewModel(app!!) {
    private val _stories: MutableLiveData<Array<Story>> = MutableLiveData()
    val stories: LiveData<Array<Story>>
        get() = _stories
    private val _indexToView: MutableLiveData<Int> = MutableLiveData()
    val indexToView: LiveData<Int>
        get() = _indexToView
    private val _snackbarMessage: MutableLiveData<String> = MutableLiveData()
    val snackbarMessage: LiveData<String>
        get() = _snackbarMessage

    private val readStories: LinkedList<Story> = LinkedList()

    var activeStory: Story? = null
        private set

    var isActiveStoryStarred: LiveData<Boolean>? = null
        private set

    fun setActiveStory(activeStory: Story) {
        this.activeStory = activeStory
        AppExecutors.getInstance().diskIO().execute { isActiveStoryStarred = BlurbDb.getInstance(getApplication()).blurbDao().doesStoryExist(activeStory.storyHash) }
    }

    fun setStories(newStories: Array<Story>) {
        _stories.value = newStories
    }

    fun setIndexToView(newIndex: Int) {
        _indexToView.value = newIndex
    }

    fun enqueueMarkAsRead(story: Story) {
        if (story.isRead) return
        if (readStories.contains(story)) return
        story.isRead = true
        readStories.add(story)
    }

    fun removeFromMarkAsReadQueue(story: Story) {
        if (!story.isRead) return
        story.isRead = false
        readStories.remove(story)
    }

    fun markQueueAsRead() {
        if (readStories.isEmpty()) return
        try { //OkHttp is required due to an unknown amount of Hashes to mark. Boilerplate as follows:
            val builder = StringBuilder()
            for (i in readStories) {
                val encodedHash = URLEncoder.encode(i.storyHash, StandardCharsets.UTF_8.toString())
                builder.append("story_hash=").append(encodedHash).append('&')
            }
            builder.deleteCharAt(builder.length - 1)
            val type: MediaType = "application/x-www-form-urlencoded".toMediaType()
            val request = Request.Builder()
                    .url(Singletons.BASE_URL + "reader/mark_story_hashes_as_read")
                    .post(builder.toString().toRequestBody(type))
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .build()
            viewModelScope.launch {
                try {
                    val response = getOkHttpClient(getApplication()).newCall(request).await()
                    if (response.isSuccessful) {
                        Timber.d("Marked queue as read.")
                        readStories.clear()
                    } else {
                        Toast.makeText(getApplication(), getApplication<Application>()
                                .getString(R.string.http_error, response.code), Toast.LENGTH_SHORT).show()
                        Timber.w("Could not mark as read. HTTP Error %o", response.code)
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: UnsupportedEncodingException) {
            Timber.e(e)
            Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    fun markStoryAsStarred(story: Story) {
        viewModelScope.launch {
            try {
                val response = getNewsBlurAPI(getApplication()).markStoryAsStarred(story.storyHash)
                if (response.isSuccessful){
                    BlurbDb.getInstance(getApplication()).blurbDao().addStory(story)
                } else {
                    _snackbarMessage.postValue(getApplication<Application>().getString(R.string.http_error, response.code()))
                    Timber.w("Could not mark as starred. HTTP Error %o", response.code())
                }
            } catch (e: Exception) {
                _snackbarMessage.postValue(e.localizedMessage)
                Timber.e(e)
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    fun removeStoryFromStarred(story: Story) {
        viewModelScope.launch {
            try {
                val response = getNewsBlurAPI(getApplication()).removeStarredStory(story.storyHash)
                if (response.isSuccessful) {
                    BlurbDb.getInstance(getApplication()).blurbDao().removeStory(story)
                } else {
                    _snackbarMessage.postValue(getApplication<Application>().getString(R.string.http_error, response.code()))
                    Timber.w("Could not remove from starred. HTTP Error %o", response.code())
                }
            } catch (e: Exception) {
                _snackbarMessage.postValue(e.localizedMessage)
                Timber.e(e)
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

}