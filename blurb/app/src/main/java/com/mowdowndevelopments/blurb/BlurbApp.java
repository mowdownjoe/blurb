package com.mowdowndevelopments.blurb;

import android.app.Application;

import timber.log.Timber;

public class BlurbApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }
}
