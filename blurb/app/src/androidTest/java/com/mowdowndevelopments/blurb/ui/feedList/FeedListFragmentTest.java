package com.mowdowndevelopments.blurb.ui.feedList;

import androidx.fragment.app.testing.FragmentScenario;

import com.mowdowndevelopments.blurb.R;

import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class FeedListFragmentTest {

    @Test
    public void uiIsDisplayed(){
        FragmentScenario<FeedListFragment> scenario = FragmentScenario.launchInContainer(FeedListFragment.class);

        onView(withId(R.id.rv_feed_list)).check(matches(isDisplayed()));
        onView(withId(R.id.mi_view_all_feeds)).check(matches(isDisplayed()));
    }

}