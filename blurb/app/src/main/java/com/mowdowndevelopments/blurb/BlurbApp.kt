package com.mowdowndevelopments.blurb

import android.app.Application
import timber.log.Timber
import timber.log.Timber.DebugTree

class BlurbApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }
}