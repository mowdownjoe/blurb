package com.mowdowndevelopments.blurb;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleEqualsTest {

    @Test
    public void objectEqualsTrue(){
        Object testObject = true;

        assertEquals(Boolean.TRUE, testObject);
        assertEquals(testObject, Boolean.TRUE);
    }
}
