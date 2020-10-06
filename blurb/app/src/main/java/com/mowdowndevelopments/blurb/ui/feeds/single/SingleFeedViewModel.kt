package com.mowdowndevelopments.blurb.ui.feeds.single

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.database.entities.Story
import com.mowdowndevelopments.blurb.network.LoadingStatus
import com.mowdowndevelopments.blurb.network.Singletons
import com.mowdowndevelopments.blurb.network.Singletons.getOkHttpClient
import com.mowdowndevelopments.blurb.ui.feeds.BaseFeedViewModel
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

class SingleFeedViewModel(app: Application, feedId: Int) : BaseFeedViewModel(app) {
    companion object {
        private const val PAGE_SIZE = 6
    }

    val pageLoadingStatus: LiveData<LoadingStatus>
    val storyList: LiveData<PagedList<Story>>
    private val factory: SingleFeedDataSource.Factory
    override val loadingStatus: LiveData<LoadingStatus>
        get() = Transformations
                .switchMap(factory.mostRecentDataSource, SingleFeedDataSource::initialLoadingStatus)
    override val errorMessage: LiveData<String>
        get() = Transformations
                .switchMap(factory.mostRecentDataSource, SingleFeedDataSource::errorMessage)

    init {
        Timber.d("Initializing ViewModel")
        val prefs = app.getSharedPreferences(app.getString(R.string.shared_pref_file), 0)
        val sortOrder = prefs.getString(app.getString(R.string.pref_filter_key), "newest")
        val filter = prefs.getString(app.getString(R.string.pref_sort_key), "unread")
        factory = SingleFeedDataSource.Factory(getApplication(), feedId, sortOrder!!, filter!!)
        storyList = LivePagedListBuilder(factory, PAGE_SIZE).build()
        pageLoadingStatus = Transformations
                .switchMap(factory.mostRecentDataSource, SingleFeedDataSource::pageLoadingStatus)
    }

    fun simpleRefresh() {
        requireNotNull(factory.mostRecentDataSource.value).invalidate()
    }

    fun refreshWithNewParameters(sortOrder: String, filter: String) {
        requireNotNull(factory.mostRecentDataSource.value)
                .resetWithNewParameters(sortOrder, filter)
    }

    fun markAllAsRead() {
        val stringBuilder: StringBuilder = StringBuilder()
        try {
            val stories: List<Story> = requireNotNull(storyList.value)
            for (i in stories) {
                val encodedHash = URLEncoder.encode(i.storyHash, StandardCharsets.UTF_8.toString())
                stringBuilder.append("story_hash=").append(encodedHash).append('&')
            }
            stringBuilder.deleteCharAt(stringBuilder.length - 1)
        } catch (e: UnsupportedEncodingException) {
            Timber.e(e)
            val errorMsg = getApplication<Application>().getString(R.string.err_fail_mark_all) + e.localizedMessage
            Toast.makeText(getApplication(), errorMsg, Toast.LENGTH_LONG).show()
            FirebaseCrashlytics.getInstance().recordException(e)
            return
        }
        val type: MediaType = "application/x-www-form-urlencoded".toMediaType()
        val request = Request.Builder()
                .url(Singletons.BASE_URL + "reader/mark_story_hashes_as_read")
                .post(stringBuilder.toString().toRequestBody(type))
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build()

        viewModelScope.launch {
            try {
                val response = getOkHttpClient(getApplication()).newCall(request).await()
                if (response.isSuccessful){
                    Timber.d("Marked feed as read.")
                } else {
                    Toast.makeText(getApplication(), getApplication<Application>()
                            .getString(R.string.http_error, response.code), Toast.LENGTH_SHORT).show()
                    Timber.w("Could not mark as read. HTTP Error ${response.code}")
                }
            } catch (e: Exception) {
                Timber.e(e)
                FirebaseCrashlytics.getInstance().recordException(e)
                Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    internal class Factory(private val app: Application, private val feedId: Int) : AndroidViewModelFactory(app) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SingleFeedViewModel::class.java)) {
                return SingleFeedViewModel(app, feedId) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }

        init {
            Timber.d("Creating Factory for ViewModel")
        }
    }
}