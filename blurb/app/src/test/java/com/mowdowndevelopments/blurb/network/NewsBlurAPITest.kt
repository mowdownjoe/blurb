package com.mowdowndevelopments.blurb.network

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.mowdowndevelopments.blurb.database.entities.Feed
import com.mowdowndevelopments.blurb.database.entities.Story
import com.mowdowndevelopments.blurb.network.Singletons.getNewsBlurAPI
import com.mowdowndevelopments.blurb.network.Singletons.moshi
import com.mowdowndevelopments.blurb.network.responseModels.FeedContentsResponse
import com.mowdowndevelopments.blurb.network.responseModels.GetFeedsResponse
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response
import java.io.IOException
import java.util.*

@RunWith(RobolectricTestRunner::class)
class NewsBlurAPITest {
    private lateinit var server: MockWebServer
    @Before
    @Throws(Exception::class)
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        server.shutdown()
    }

    //GIVEN
    @Test
    fun getFeeds(){
            //GIVEN
            val testFolders = HashMap<String, Array<Int>>()
            val testFeeds = HashMap<String, Feed>()
            val folderKey = "test folder"
            testFolders[folderKey] = arrayOf(1, 2, 3)
            testFeeds["test feed"] = Feed(42, "pls ignore",
                    "http://testfeed.plsignore",
                    "http://testfeed.plsignore",
                    "http://testfeed.plsignore/img.png", 5, 5)
            val feedsResponse = GetFeedsResponse(testFolders, testFeeds)

            //WHEN
            val response: Response<GetFeedsResponse>
            response = try {
                val json = moshi?.adapter(GetFeedsResponse::class.java)?.toJson(feedsResponse)
                server.enqueue(MockResponse().setResponseCode(200).setBody(requireNotNull(json)))
                getNewsBlurAPI(ApplicationProvider.getApplicationContext(),
                        server.url("/").toString()).getFeeds().execute()
            } catch (e: IOException) {
                fail(e.message)
                e.printStackTrace()
                return
            }

            //THEN
            if (response.isSuccessful) {
                assertThat(response.body()).isNotNull()
                assertThat(response.body()!!.feeds).isNotNull()
                assertThat(response.body()!!.feeds).containsAtLeastEntriesIn(testFeeds)
                assertThat(response.body()!!.folders).isNotNull()
                assertThat(response.body()!!.folders).containsKey(folderKey)
                assertThat(response.body()!!.folders[folderKey]).asList().containsAtLeast(1, 2, 3)
            } else {
                fail("Server did not return successful response")
            }
        }

    @Test
    fun getFeedContents() {
        //GIVEN
        val testStory1 = Story("1a4:45", "hi", "test post pls ignore", "1234567890", "nobody", "testpost.pls/ignore", 42)
        val testStory2 = Story("45:1a4", "hi", "lorem ipsum", "12345678910", "blah", "blah.blah", 42)
        val response = FeedContentsResponse(arrayOf(testStory1, testStory2))

        //WHEN
        val json = moshi!!.adapter(FeedContentsResponse::class.java).toJson(response)
        server.enqueue(MockResponse().setBody(json).setResponseCode(200))
        val serverResponse: Response<FeedContentsResponse>
        serverResponse = try {
            getNewsBlurAPI(ApplicationProvider.getApplicationContext(),
                    server.url("/").toString())
                    .getFeedContents(42, "all", "oldest").execute()
        } catch (e: IOException) {
            e.printStackTrace()
            fail(e.message)
            return
        }

        //THEN
        if (serverResponse.isSuccessful) {
            assertThat(serverResponse.body()).isNotNull()
            assertThat(serverResponse.body()!!.stories).asList().containsExactly(testStory1, testStory2)
        } else {
            fail("Server did not return successful response")
        }
    }
}