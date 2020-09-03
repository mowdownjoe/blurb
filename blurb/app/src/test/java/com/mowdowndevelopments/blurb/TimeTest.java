package com.mowdowndevelopments.blurb;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.google.common.truth.Truth.assertThat;

public class TimeTest {

    @Test
    public void instantTest(){
        Instant instant = Instant.ofEpochSecond(1599162080);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        assertThat(dateTime.getMinute()).isEqualTo(41);
        assertThat(dateTime.getHour()).isEqualTo(15);
        assertThat(dateTime.getDayOfMonth()).isEqualTo(3);
        assertThat(dateTime.getMonthValue()).isEqualTo(9);
        assertThat(dateTime.getYear()).isEqualTo(2020);
    }
}
