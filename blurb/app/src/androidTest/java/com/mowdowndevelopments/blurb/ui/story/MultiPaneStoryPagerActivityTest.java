package com.mowdowndevelopments.blurb.ui.story;

/*
 * UI Tests intended for Tablet (multi-pane) UI. These tests may not behave properly if used
 * on a non-Tablet device.
 */

import android.content.Intent;
import android.os.Bundle;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.entities.Story;

import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webContent;
import static androidx.test.espresso.web.matcher.DomMatchers.withBody;
import static androidx.test.espresso.web.matcher.DomMatchers.withTextContent;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static com.google.common.truth.Truth.assertThat;

public class MultiPaneStoryPagerActivityTest {

    @Test
    public void multipleStories_DisplayedInUi(){
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
        scenario.onActivity(activity -> assertThat(activity.binding.fcvMultipaneList).isNotNull());
        onView(withId(R.id.rv_stories_mp)).check(matches(withChild(withChild(withText(testStory2.getTitle())))));
        onView(withId(R.id.rv_stories_mp)).check(matches(withChild(withChild(withText(testStory.getTitle())))));

        onView(withId(R.id.tv_story_title)).check(matches(withText(testStory.getTitle())));
        onView(withId(R.id.tv_story_author)).check(matches(withText(testStory.getAuthors())));
        onWebView(withId(R.id.wv_story_content)).forceJavascriptEnabled()
                .check(webContent(withBody(withTextContent(testStory.getContent()))));
    }

    @Test
    public void multipleStories_ClickInList_DisplayNewStory(){
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
        scenario.onActivity(activity -> assertThat(activity.binding.fcvMultipaneList).isNotNull());
        onView(withId(R.id.rv_stories_mp)).check(matches(withChild(withChild(withText(testStory2.getTitle())))));
        onView(withId(R.id.rv_stories_mp)).check(matches(withChild(withChild(withText(testStory.getTitle())))));

        onView(withId(R.id.tv_story_title)).check(matches(withText(testStory.getTitle())));
        onView(withId(R.id.tv_story_author)).check(matches(withText(testStory.getAuthors())));
        onWebView(withId(R.id.wv_story_content)).forceJavascriptEnabled()
                .check(webContent(withBody(withTextContent(testStory.getContent()))));

        onView(withId(R.id.rv_stories_mp)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        onView(withId(R.id.tv_story_title)).check(matches(withText(testStory2.getTitle())));
        onView(withId(R.id.tv_story_author)).check(matches(withText(testStory2.getAuthors())));
        onWebView(withId(R.id.wv_story_content)).forceJavascriptEnabled()
                .check(webContent(withBody(withTextContent(testStory2.getContent()))));
    }
}
