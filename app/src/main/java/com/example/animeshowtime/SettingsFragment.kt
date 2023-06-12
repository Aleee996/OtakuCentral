package com.example.animeshowtime

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import androidx.preference.SwitchPreferenceCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import kotlin.time.Duration

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<ListPreference>("notificationInterval")?.setOnPreferenceChangeListener { preference: Preference, newValue: Any ->
            val oldInterval = preference.sharedPreferences?.getString("notificationInterval", "24h") ?: "24h"
            if ((newValue as String) != oldInterval) {
                (activity as MainActivity).createNotificationWorker(
                    Duration.parse(newValue),
                    ExistingPeriodicWorkPolicy.UPDATE
                )
                return@setOnPreferenceChangeListener true
            }
            else return@setOnPreferenceChangeListener false
        }

        findPreference<SwitchPreferenceCompat>("notificationOn")?.setOnPreferenceChangeListener { preference: Preference, newValue: Any ->
            if (newValue == true) {
                (activity as MainActivity).createNotificationWorker(
                    Duration.parse(preference.sharedPreferences?.getString("notificationInterval", "24h") ?: "24h"),
                    ExistingPeriodicWorkPolicy.UPDATE
                )
                return@setOnPreferenceChangeListener true
            }
            else {
                context?.let { WorkManager.getInstance(it).cancelUniqueWork("notify") }
                return@setOnPreferenceChangeListener true
            }
        }
    }

}