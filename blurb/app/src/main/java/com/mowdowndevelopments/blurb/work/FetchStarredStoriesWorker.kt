package com.mowdowndevelopments.blurb.work

import android.content.Context
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.database.BlurbDb
import com.mowdowndevelopments.blurb.network.Singletons
import com.mowdowndevelopments.blurb.network.responseModels.FeedContentsResponse
import okhttp3.HttpUrl
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import timber.log.Timber

class FetchStarredStoriesWorker(c: Context, workerParams: WorkerParameters) : CoroutineWorker(c, workerParams) {
    companion object {
        const val WORK_TAG = "com.mowdowndevelopments.blurb.FETCH_STORIES"
    }

    override suspend fun doWork(): Result {
        return try {
            val hashResponse = Singletons.getNewsBlurAPI(applicationContext).getStarredStoryHashes()
            if (hashResponse.isSuccessful){
                val body = hashResponse.body()
                requireNotNull(body) {
                    //If does not meet requirements:
                    return@doWork Result.failure()
                }
                fetchStoriesAndCommitToDb(body.starredStoryHashes)
            } else {
                Result.failure()
            }
        } catch (e: Exception){
            val errorMsg = applicationContext.getString(R.string.error_star_fetch)
            Timber.e(e, "$errorMsg${e.message}")
            Toast.makeText(applicationContext, errorMsg + e.localizedMessage, Toast.LENGTH_LONG).show()
            Result.failure()
        }
    }

    private suspend fun fetchStoriesAndCommitToDb(hashes: List<String>): Result {
        require(hashes.isNotEmpty()) {
            //If does not meet requirements:
            return@fetchStoriesAndCommitToDb Result.success()
        }
        val builder = HttpUrl.Builder().run {
            scheme("https")
            host("newsblur.com")
            addPathSegments("reader/starred_stories")
        }
        for ((i, storyHash) in hashes.withIndex()) {
            if (i >= 100) break //Only accepts max 100 hashes.
            builder.addQueryParameter("h", storyHash)
        }
        val request = Request.Builder().run {
            url(builder.build())
            get()
            build()
        }
        return try {
            val response = Singletons.getOkHttpClient(applicationContext).newCall(request).await()
            if (response.isSuccessful){
                val body = Singletons.moshi
                        .adapter(FeedContentsResponse::class.java)
                        .fromJson(requireNotNull(response.body).string())
                BlurbDb.getInstance(applicationContext).blurbDao()
                        .addStories(listOf(*requireNotNull(body).stories))
                response.body?.close()
                Result.success()
            } else {
                val errorMsg = (applicationContext.getString(R.string.error_star_fetch)
                        + applicationContext.getString(R.string.http_error, response.code))
                Timber.e(errorMsg)
                Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
                Result.failure()
            }
        } catch (e: Exception) {
            val errorMsg = applicationContext.getString(R.string.error_star_fetch)
            Timber.e(e, "%s%s", errorMsg, e.message)
            Toast.makeText(applicationContext, errorMsg + e.localizedMessage, Toast.LENGTH_LONG).show()
            Result.failure()
        }
    }
}