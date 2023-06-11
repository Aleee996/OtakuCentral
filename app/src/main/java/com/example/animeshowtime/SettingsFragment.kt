package com.example.animeshowtime

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.ExistingPeriodicWorkPolicy
import kotlin.time.Duration

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<ListPreference>("notificationInterval")?.setOnPreferenceChangeListener { preference: Preference, newValue: Any ->
            val oldInterval = preference.sharedPreferences?.getString("notificationInterval", "") ?: "24h"
            if ((newValue as String) != oldInterval) {
                (activity as MainActivity).createNotificationWorker(
                    Duration.parse(newValue),
                    ExistingPeriodicWorkPolicy.UPDATE
                )
                return@setOnPreferenceChangeListener true
            }
            else return@setOnPreferenceChangeListener false
        }
    }

}