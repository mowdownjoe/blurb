package com.mowdowndevelopments.blurb.ui.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mowdowndevelopments.blurb.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val crashlyticsPref = findPreference<SwitchPreferenceCompat>(getString(R.string.pref_crashlytics_key))
        crashlyticsPref?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
            if (true == newValue) {
                FirebaseCrashlytics.getInstance().sendUnsentReports()
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            } else {
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
            }
            true
        }
    }
}