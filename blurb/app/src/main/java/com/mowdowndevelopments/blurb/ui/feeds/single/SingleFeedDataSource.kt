package com.mowdowndevelopments.blurb.ui.feeds.single

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.database.entities.Story
import com.mowdowndevelopments.blurb.network.LoadingStatus
import com.mowdowndevelopments.blurb.network.Singletons.getNewsBlurAPI
import com.mowdowndevelopments.blurb.network.responseModels.FeedContentsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class SingleFeedDataSource(private val context: Context,
                           private val feedId: Int,
                           private var sortOrder: String,
                           private var filter: String) : PageKeyedDataSource<Int, Story>() {
    private val _pageLoadingStatus: MutableLiveData<LoadingStatus> = MutableLiveData(LoadingStatus.WAITING)
    val pageLoadingStatus: LiveData<LoadingStatus>
        get() = _pageLoadingStatus
    private val _initialLoadingStatus: MutableLiveData<LoadingStatus> = MutableLiveData(LoadingStatus.WAITING)
    val initialLoadingStatus: LiveData<LoadingStatus>
        get() = _initialLoadingStatus
    private val _errorMessage: MutableLiveData<String> = MutableLiveData()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    fun resetWithNewParameters(newSortOrder: String, newFilter: String) {
        sortOrder = newSortOrder
        filter = newFilter
        invalidate()
    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Story?>) {
        _initialLoadingStatus.postValue(LoadingStatus.LOADING)
        getNewsBlurAPI(context).getFeedContents(feedId, filter, sortOrder)
                .enqueue(object : Callback<FeedContentsResponse?> {
                    override fun onResponse(call: Call<FeedContentsResponse?>, response: Response<FeedContentsResponse?>) {
                        Timber.d("Successfully received response. Response Code: %o", response.code())
                        if (response.isSuccessful) {
                            _initialLoadingStatus.postValue(LoadingStatus.DONE)
                            val body = requireNotNull(response.body())
                            callback.onResult(listOf(*body.stories), null, 2)
                        } else {
                            _initialLoadingStatus.postValue(LoadingStatus.ERROR)
                            _errorMessage.postValue(context.getString(R.string.http_error, response.code()))
                        }
                    }

                    override fun onFailure(call: Call<FeedContentsResponse?>, t: Throwable) {
                        _initialLoadingStatus.postValue(LoadingStatus.ERROR)
                        _errorMessage.postValue(t.localizedMessage)
                        FirebaseCrashlytics.getInstance().recordException(t)
                        Timber.e(t)
                    }
                })
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Story?>) {
        _pageLoadingStatus.postValue(LoadingStatus.LOADING)
        getNewsBlurAPI(context).getFeedContents(feedId, filter, sortOrder, params.key)
                .enqueue(object : Callback<FeedContentsResponse?> {
                    override fun onResponse(call: Call<FeedContentsResponse?>, response: Response<FeedContentsResponse?>) {
                        Timber.d("Successfully received response. Response Code: %o", response.code())
                        if (response.isSuccessful) {
                            _pageLoadingStatus.postValue(LoadingStatus.DONE)
                            val stories = requireNotNull(response.body()).stories
                            if (stories.isNotEmpty()) {
                                callback.onResult(listOf(*stories), params.key + 1)
                            } else {
                                callback.onResult(listOf(*stories), null)
                            }
                        } else {
                            _pageLoadingStatus.postValue(LoadingStatus.ERROR)
                            _errorMessage.postValue(context.getString(R.string.http_error, response.code()))
                        }
                    }

                    override fun onFailure(call: Call<FeedContentsResponse?>, t: Throwable) {
                        _pageLoadingStatus.postValue(LoadingStatus.ERROR)
                        _errorMessage.postValue(t.localizedMessage)
                        FirebaseCrashlytics.getInstance().recordException(t)
                        Timber.e(t)
                    }
                })
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Story>) {
        //Will only append to initial load, not prepend
    }

    internal class Factory(private val context: Context, private val feedId: Int, private val sortOrder: String, private val filter: String) : DataSource.Factory<Int, Story>() {
        private val _mostRecentDataSource: MutableLiveData<SingleFeedDataSource> = MutableLiveData()
        val mostRecentDataSource: LiveData<SingleFeedDataSource>
            get() = _mostRecentDataSource

        override fun create(): SingleFeedDataSource {
            val dataSource = SingleFeedDataSource(context, feedId, sortOrder, filter)
            _mostRecentDataSource.postValue(dataSource)
            return dataSource
        }

    }

}