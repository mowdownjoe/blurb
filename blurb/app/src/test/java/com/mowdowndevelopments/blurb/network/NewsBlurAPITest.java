package com.mowdowndevelopments.blurb.network;

import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.network.ResponseModels.FeedContentsResponse;
import com.mowdowndevelopments.blurb.network.ResponseModels.GetFeedsResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.CookieHandler;
import java.util.HashMap;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Response;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

public class NewsBlurAPITest {

    private MockWebServer server;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        TestCookieHandler cookieHandler = new TestCookieHandler();
        CookieHandler.setDefault(cookieHandler);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
        CookieHandler.setDefault(null);
    }

    @Test
    public void getFeeds() {
        //GIVEN
        HashMap<String, Integer[]> testFolders = new HashMap<>();
        HashMap<String, Feed> testFeeds = new HashMap<>();
        String folderKey = "test folder";
        testFolders.put(folderKey, new Integer[]{1,2,3});
        testFeeds.put("test feed", new Feed(42, "pls ignore",
                "http://testfeed.plsignore",
                "http://testfeed.plsignore",
                "http://testfeed.plsignore/img.png", 5, 5));
        GetFeedsResponse feedsResponse = new GetFeedsResponse(testFolders, testFeeds);

        //WHEN
        Response<GetFeedsResponse> response;
        try {
            String json = Singletons.getMoshi().adapter(GetFeedsResponse.class).toJson(feedsResponse);
            server.enqueue(new MockResponse().setResponseCode(200).setBody(json));

            response = Singletons
                    .getNewsBlurAPI(server.url("/").toString()).getFeeds().execute();
        } catch (IOException e) {
            fail(e.getMessage());
            e.printStackTrace();
            return;
        }

        //THEN
        if (response.isSuccessful()){
            assertThat(response.body()).isNotNull();
            assertThat(response.body().getFeeds()).isNotNull();
            assertThat(response.body().getFeeds()).containsAtLeastEntriesIn(testFeeds);
            assertThat(response.body().getFolders()).isNotNull();
            assertThat(response.body().getFolders()).containsKey(folderKey);
            assertThat(response.body().getFolders().get(folderKey)).asList().containsAtLeast(1,2,3);
        } else {
            fail("Server did not return successful response");
        }
    }

    @Test
    public void getFeedContents() {
        //GIVEN
        Story testStory1 = new Story("1a4:45","hi","test post pls ignore", "1234567890", "nobody", "testpost.pls/ignore", 42);
        Story testStory2 = new Story("45:1a4","hi","lorem ipsum", "12345678910", "blah", "blah.blah", 42);
        FeedContentsResponse response = new FeedContentsResponse(new Story[]{testStory1, testStory2});

        //WHEN
        String json = Singletons.getMoshi().adapter(FeedContentsResponse.class).toJson(response);
        server.enqueue(new MockResponse().setBody(json).setResponseCode(200));
        Response<FeedContentsResponse> serverResponse;
        try {
            serverResponse = Singletons.getNewsBlurAPI(server.url("/").toString())
                    .getFeedContents(42, "all", true).execute();
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
            return;
        }

        //THEN
        if (serverResponse.isSuccessful()){
            assertThat(serverResponse.body()).isNotNull();
            assertThat(serverResponse.body().getStories()).asList().containsExactly(testStory1, testStory2);
        } else {
            fail("Server did not return successful response");
        }
    }
}