package com.mowdowndevelopments.blurb.ui.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.mowdowndevelopments.blurb.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        SwitchPreferenceCompat crashlyticsPref = findPreference(getString(R.string.pref_crashlytics_key));
        crashlyticsPref.setOnPreferenceChangeListener((preference, newValue) -> {
            if (Boolean.TRUE.equals(newValue)){
                FirebaseCrashlytics.getInstance().sendUnsentReports();
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
            } else {
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false);
            }
            return true;
        });
    }
}