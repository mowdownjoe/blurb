package com.mowdowndevelopments.blurb.network;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class TestCookieHandler extends CookieHandler {
    @Override
    public Map<String, List<String>> get(URI uri, Map<String, List<String>> map) throws IOException {
        return map;
    }

    @Override
    public void put(URI uri, Map<String, List<String>> map) throws IOException {
        //Stub
    }
}
