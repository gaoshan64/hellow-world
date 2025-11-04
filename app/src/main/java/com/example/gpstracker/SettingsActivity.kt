package com.example.gpstracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.example.gpstracker.util.LanguageUtils

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setSupportActionBar(findViewById(R.id.settingsToolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settingsContainer, SettingsFragment())
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            configureUnitPreferences()
            configureLanguagePreference()
        }

        private fun configureUnitPreferences() {
            findPreference<ListPreference>(MainActivity.PREF_ALTITUDE_UNIT)?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            findPreference<ListPreference>(MainActivity.PREF_SPEED_UNIT)?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        }

        private fun configureLanguagePreference() {
            val languagePreference = findPreference<ListPreference>(LANGUAGE_KEY) ?: return
            languagePreference.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            languagePreference.setOnPreferenceChangeListener { _, newValue ->
                LanguageUtils.applyLanguagePreference(newValue as String)
                true
            }
        }
    }

    companion object {
        const val LANGUAGE_KEY = "language"
    }
}
