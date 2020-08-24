package com.mowdowndevelopments.blurb.ui.story;

import android.os.Bundle;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.entities.Story;

import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webContent;
import static androidx.test.espresso.web.matcher.DomMatchers.withBody;
import static androidx.test.espresso.web.matcher.DomMatchers.withTextContent;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static org.hamcrest.Matchers.containsString;

public class StoryFragmentTest {

    @Test
    public void storyContent_DisplayedInUi(){
        //GIVEN
        Story testStory = new Story(
                "123456:654321",
                "html here",
                "test story pls ignore",
                "123456789",
                "Nobody",
                "127.0.0.1"
        );

        //WHEN
        Bundle bundle = new Bundle();
        bundle.putParcelable(StoryFragment.ARG_STORY, testStory);
        FragmentScenario<StoryFragment> scenario =
                FragmentScenario.launchInContainer(StoryFragment.class, bundle, R.style.AppTheme, new FragmentFactory());

        //THEN
        onView(withId(R.id.tv_story_title)).check(matches(withText(testStory.getTitle())));
        onView(withId(R.id.tv_story_author)).check(matches(withText(testStory.getAuthors())));
        onWebView(withId(R.id.wv_story_content)).forceJavascriptEnabled()
                .check(webContent(withBody(withTextContent(testStory.getContent()))));
    }

    @Test
    public void storyContent_HtmlParsed_DisplayedInUi() throws InterruptedException {
        //GIVEN
        Story testStory = new Story(
                "123456:654321",
                "<p align=\\\"center\\\"><a href=\\\"https://www.anandtech.com/show/15942/lian-li-unveils-its-first-aio-coolers-the-galahad-360-and-240-with-argb\\\"><img alt=\\\"\\\" src=\\\"https://images.anandtech.com/doci/15942/Capture One Session11104_575px.jpg\\\" /></a></p><p><p>Lian Li is highly regarded for its elegant and premium aluminium chassis, most recently its entry-level O11 Dynamic XL E-ATX case, which has become one of its most popular ranges. Its latest product marks the companies first foray into the liquid cooling market with the Galahad AIO series, with two different sizes available, including 240 and 360 mm.&nbsp;</p>\n</p><p align=\\\"center\\\"><a href=\\\"http://dynamic1.anandtech.com/www/delivery/ck.php?n=a1f2f01f&amp;cb=760016681\\\" target=\\\"_blank\\\"><img alt=\\\"\\\" border=\\\"0\\\" src=\\\"http://dynamic1.anandtech.com/www/delivery/avw.php?zoneid=24&amp;cb=760016681&amp;n=a1f2f01f\\\" /></a><img alt=\\\"\\\" border=\\\"0\\\" height=\\\"1\\\" src=\\\"http://toptenreviews.122.2o7.net/b/ss/tmn-test/1/H.27.3--NS/0\\\" width=\\\"1\\\" /></p>",
                "test story pls ignore",
                "123456789",
                "Nobody",
                "127.0.0.1"
        );

        //WHEN
        Bundle bundle = new Bundle();
        bundle.putParcelable(StoryFragment.ARG_STORY, testStory);
        FragmentScenario<StoryFragment> scenario =
                FragmentScenario.launchInContainer(StoryFragment.class, bundle, R.style.AppTheme, new FragmentFactory());

        Thread.sleep(5000);

        //THEN
        onView(withId(R.id.tv_story_title)).check(matches(withText(testStory.getTitle())));
        onView(withId(R.id.tv_story_author)).check(matches(withText(testStory.getAuthors())));
        onWebView(withId(R.id.wv_story_content)).forceJavascriptEnabled()
                .check(webContent(withBody(withTextContent(containsString("Dynamic XL E-ATX")))));
    }

}