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
import static androidx.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static androidx.test.espresso.web.model.Atoms.getTitle;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static org.hamcrest.CoreMatchers.containsString;

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
        onWebView(withId(R.id.wv_story_content)).check(webMatches(getTitle(), containsString(testStory.getContent())));
        //TODO Learn Espresso Web API
    }

}