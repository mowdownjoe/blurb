package com.mowdowndevelopments.blurb.ui.feedList

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.database.BlurbDb.Companion.getInstance
import com.mowdowndevelopments.blurb.network.LoadingStatus
import com.mowdowndevelopments.blurb.network.Singletons.getNewsBlurAPI
import com.mowdowndevelopments.blurb.network.responseModels.GetFeedsResponse
import com.mowdowndevelopments.blurb.work.FetchStarredStoriesWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class FeedListViewModel(application: Application) : AndroidViewModel(application) {
    private var refreshing = false
    private val feedsResponseData: MutableLiveData<GetFeedsResponse> = MutableLiveData()
    private val status: MutableLiveData<LoadingStatus> = MutableLiveData(LoadingStatus.WAITING)
    private val errorMessage: MutableLiveData<String> = MutableLiveData()
    private val newFeedCallback: Callback<Map<String, Any>> = object : Callback<Map<String, Any>> {
        override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
            refreshing = false
            if (response.isSuccessful) {
                refreshFeeds()
            } else {
                val errorMsg = getApplication<Application>().getString(R.string.http_error, response.code())
                errorMessage.postValue(errorMsg)
                Timber.e("loadFeeds.onResponse: %s", errorMsg)
            }
        }

        override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
            refreshing = false
            errorMessage.postValue(t.localizedMessage)
            Timber.e(t, "loadFeeds.onFailure: %s", t.message)
            FirebaseCrashlytics.getInstance().log(String.format("loginCallback.onFailure: %s", t.message))
            FirebaseCrashlytics.getInstance().recordException(t)
        }
    }

    fun getFeedsResponseData(): LiveData<GetFeedsResponse?> {
        return feedsResponseData
    }

    val loadingStatus: LiveData<LoadingStatus>
        get() = status

    fun getErrorMessage(): LiveData<String> {
        return errorMessage
    }

    fun loadFeeds() {
        if (status.value === LoadingStatus.LOADING) return
        if (refreshing) return
        Timber.d("Loading feeds.")
        status.postValue(LoadingStatus.LOADING)
        getNewsBlurAPI(getApplication()).getFeeds().enqueue(object : Callback<GetFeedsResponse?> {
            override fun onResponse(call: Call<GetFeedsResponse?>, response: Response<GetFeedsResponse?>) {
                if (response.isSuccessful) {
                    status.postValue(LoadingStatus.DONE)
                    feedsResponseData.postValue(response.body())
                    Timber.d("Feeds loaded.")
                } else {
                    status.postValue(LoadingStatus.ERROR)
                    val errorMsg = getApplication<Application>().getString(R.string.http_error, response.code())
                    errorMessage.postValue(errorMsg)
                    Timber.e("loadFeeds.onResponse: %s", errorMsg)
                }
            }

            override fun onFailure(call: Call<GetFeedsResponse?>, t: Throwable) {
                status.postValue(LoadingStatus.ERROR)
                errorMessage.postValue(t.localizedMessage)
                Timber.e(t, "loadFeeds.onFailure: %s", t.message)
                FirebaseCrashlytics.getInstance().log(String.format("loginCallback.onFailure: %s", t.message))
                FirebaseCrashlytics.getInstance().recordException(t)
            }
        })
    }

    @JvmOverloads
    fun postFeedsToDb(feedData: GetFeedsResponse = requireNotNull(feedsResponseData.value)) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = OneTimeWorkRequest.Builder(FetchStarredStoriesWorker::class.java)
                        .addTag(FetchStarredStoriesWorker.WORK_TAG)
                        .build()
                getInstance(getApplication()).blurbDao().addFeeds(feedData.feeds.values)
                with(Dispatchers.Main){
                    WorkManager.getInstance(getApplication()).enqueue(request)
                }
            } catch (e: SQLiteConstraintException) {
                Timber.w(e)
            }
        }
    }

    fun refreshFeeds() {
        if (status.value === LoadingStatus.LOADING) return
        if (refreshing) return
        refreshing = true
        getNewsBlurAPI(getApplication()).getFeedsAndRefreshCounts().enqueue(object : Callback<GetFeedsResponse?> {
            override fun onResponse(call: Call<GetFeedsResponse?>, response: Response<GetFeedsResponse?>) {
                refreshing = false
                if (response.isSuccessful) {
                    feedsResponseData.postValue(response.body())
                } else {
                    val errorMsg = getApplication<Application>().getString(R.string.http_error, response.code())
                    errorMessage.postValue(errorMsg)
                    Timber.e("loadFeeds.onResponse: %s", errorMsg)
                }
            }

            override fun onFailure(call: Call<GetFeedsResponse?>, t: Throwable) {
                refreshing = false
                errorMessage.postValue(t.localizedMessage)
                Timber.e(t, "loadFeeds.onFailure: %s", t.message)
            }
        })
    }

    fun addNewFeed(url: String?) {
        if (status.value === LoadingStatus.LOADING) return
        if (refreshing) return
        refreshing = true
        getNewsBlurAPI(getApplication()).addNewFeed(url!!).enqueue(newFeedCallback)
    }

    fun addNewFeed(url: String?, folder: String?) {
        if (status.value === LoadingStatus.LOADING) return
        if (refreshing) return
        refreshing = true
        getNewsBlurAPI(getApplication()).addNewFeed(url!!, folder!!).enqueue(newFeedCallback)
    }

    fun createNewFolder(folderName: String?) {
        if (status.value === LoadingStatus.LOADING) return
        if (refreshing) return
        refreshing = true
        getNewsBlurAPI(getApplication()).createNewFolder(folderName!!).enqueue(newFeedCallback)
    }

    fun createNewFolder(folderName: String?, parentFolderName: String?) {
        if (status.value === LoadingStatus.LOADING) return
        if (refreshing) return
        refreshing = true
        getNewsBlurAPI(getApplication()).createNewFolder(folderName!!, parentFolderName!!).enqueue(newFeedCallback)
    }

}