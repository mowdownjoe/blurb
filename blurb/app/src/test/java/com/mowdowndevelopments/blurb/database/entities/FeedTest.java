package com.mowdowndevelopments.blurb.database.entities;

import android.os.Parcel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
public class FeedTest {

    @Test
    public void writeToParcel_constructSameFeedFromParcel() {
        //GIVEN
        Feed testFeed = new Feed(42, "test", "feed", "pls", "ignore");
        Parcel parcel = Parcel.obtain();

        //WHEN
        testFeed.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Feed result = Feed.CREATOR.createFromParcel(parcel);

        //THEN
        assertThat(testFeed).isEqualTo(result);
    }
}