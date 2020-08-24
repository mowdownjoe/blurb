package com.mowdowndevelopments.blurb.ui.story;

import android.content.Intent;
import android.os.Bundle;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.entities.Story;

import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webContent;
import static androidx.test.espresso.web.matcher.DomMatchers.withBody;
import static androidx.test.espresso.web.matcher.DomMatchers.withTextContent;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StoryPagerActivityTest {

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
        Bundle extras = new StoryPagerActivityArgs.Builder(new Story[]{testStory})
                .setInitialStory(0)
                .build()
                .toBundle();
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), StoryPagerActivity.class)
                .putExtras(extras);
        ActivityScenario<StoryPagerActivity> scenario = ActivityScenario.launch(intent);

        //THEN
        onView(withId(R.id.tv_story_title)).check(matches(withText(testStory.getTitle())));
        onView(withId(R.id.tv_story_author)).check(matches(withText(testStory.getAuthors())));
        onWebView(withId(R.id.wv_story_content)).forceJavascriptEnabled()
                .check(webContent(withBody(withTextContent(testStory.getContent()))));

        scenario.onActivity(activity -> {
            assertNotNull(activity.binding.vp2StoryPager.getAdapter());
            assertEquals(1, activity.binding.vp2StoryPager.getAdapter().getItemCount());
        });
    }

    @Test
    public void storyContent_MultipleStories_UiSwipesBetweenStories(){
        //GIVEN
        Story testStory = new Story(
                "123456:654321",
                "html here",
                "test story pls ignore",
                "123456789",
                "Nobody",
                "127.0.0.1"
        );
        Story testStory2 = new Story(
                "abcdef:fedcba",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
                "This is a different story",
                "12345678910",
                "Somebody",
                "192.168.1.1"
        );

        //WHEN
        Bundle extras = new StoryPagerActivityArgs.Builder(new Story[]{testStory, testStory2})
                .setInitialStory(0)
                .build()
                .toBundle();
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), StoryPagerActivity.class)
                .putExtras(extras);
        ActivityScenario<StoryPagerActivity> scenario = ActivityScenario.launch(intent);

        //THEN
        onView(withId(R.id.tv_story_title)).check(matches(withText(testStory.getTitle())));
        onView(withId(R.id.tv_story_author)).check(matches(withText(testStory.getAuthors())));
        onWebView(withId(R.id.wv_story_content)).forceJavascriptEnabled()
                .check(webContent(withBody(withTextContent(testStory.getContent()))));

        scenario.onActivity(activity -> {
            assertNotNull(activity.binding.vp2StoryPager.getAdapter());
            assertEquals(2, activity.binding.vp2StoryPager.getAdapter().getItemCount());
            activity.viewModel.removeFromMarkAsReadQueue(activity.viewModel.getActiveStory());
        });

        onView(withId(R.id.vp2_story_pager)).perform(swipeLeft());

        onView(withId(R.id.tv_story_title)).check(matches(not(withText(testStory.getTitle()))));
        onView(withId(R.id.tv_story_author)).check(matches(not(withText(testStory.getAuthors()))));
        onWebView(withId(R.id.wv_story_content)).forceJavascriptEnabled()
                .check(webContent(withBody(withTextContent(not(testStory.getContent())))));
        scenario.onActivity(activity -> {
            assertNotNull(activity.binding.vp2StoryPager.getAdapter());
            assertEquals(1, activity.binding.vp2StoryPager.getCurrentItem());
        });
    }

}