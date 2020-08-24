package com.mowdowndevelopments.blurb.ui.story;

import androidx.test.espresso.IdlingResource;
import androidx.viewpager2.widget.ViewPager2;

public class ViewPager2IdlingResource implements IdlingResource {
    private ResourceCallback callback = null;
    private ViewPager2 pager;

    public ViewPager2IdlingResource(ViewPager2 pager) {
        this.pager = pager;
    }


    @Override
    public String getName() {
        return "ViewPager2 Idling Resource";
    }

    @Override
    public boolean isIdleNow() {
        boolean idle = pager.getScrollState() == ViewPager2.SCROLL_STATE_IDLE;
        if (idle && callback != null){
            callback.onTransitionToIdle();
        }
        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.callback = callback;
    }
}
