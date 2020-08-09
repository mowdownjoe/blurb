package com.mowdowndevelopments.blurb.network;

import com.mowdowndevelopments.blurb.database.entities.Feed;
import com.mowdowndevelopments.blurb.network.ResponseModels.GetFeedsResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
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
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void getFeeds() {
        //GIVEN
        HashMap<String, Integer[]> testFolders = new HashMap<>();
        HashMap<String, Feed> testFeeds = new HashMap<>();
        testFolders.put("test folder", new Integer[]{1,2,3});
        testFeeds.put("test feed", new Feed(42, "pls ignore",
                "http://testfeed.plsignore",
                "http://testfeed.plsignore",
                "http://testfeed.plsignore/img.png"));
        GetFeedsResponse feedsResponse = new GetFeedsResponse(testFolders, testFeeds);

        //WHEN
        Response<GetFeedsResponse> response;
        try {
            String json = Singletons.getMoshi().adapter(GetFeedsResponse.class).toJson(feedsResponse);
            server.enqueue(new MockResponse().setResponseCode(200).setBody(json));
            //Failing because CookieHandler.getDefault() is returning null.
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
            assertThat(response.body().getFolders()).containsAtLeastEntriesIn(testFolders);
        } else {
            fail("Server did not return successful response");
        }
    }

    @Test
    public void getFeedContents() {
    }
}