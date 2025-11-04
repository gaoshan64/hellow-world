package com.example.gpstracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.gpstracker.databinding.ActivityMainBinding
import com.example.gpstracker.util.LanguageUtils
import com.example.gpstracker.util.UnitFormatter
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationManager: LocationManager
    private lateinit var sharedPreferences: SharedPreferences

    private var previousLocation: Location? = null
    private var lastLocation: Location? = null
    private var lastFixTimestamp: Long = 0L
    private var currentHeading: Float = 0f

    private val handler = Handler(Looper.getMainLooper())
    private val fixUpdateRunnable = object : Runnable {
        override fun run() {
            updateFixAge()
            handler.postDelayed(this, UPDATE_INTERVAL_MS)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        val language = sharedPreferences.getString(SettingsActivity.LANGUAGE_KEY, PREF_LANGUAGE_SYSTEM) ?: PREF_LANGUAGE_SYSTEM
        LanguageUtils.applyLanguagePreference(language)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
    }

    override fun onStart() {
        super.onStart()
        handler.post(fixUpdateRunnable)
        if (hasLocationPermission()) {
            startLocationUpdates()
        } else {
            requestLocationPermission()
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(fixUpdateRunnable)
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::sharedPreferences.isInitialized) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, R.string.permission_rationale, Toast.LENGTH_LONG).show()
        }
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun startLocationUpdates() {
        try {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, R.string.last_fix_unknown, Toast.LENGTH_SHORT).show()
            }
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                this
            )
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { lastKnown ->
                previousLocation = null
                lastLocation = lastKnown
                lastFixTimestamp = System.currentTimeMillis()
                currentHeading = if (lastKnown.hasBearing()) lastKnown.bearing else currentHeading
                updateHeading()
                updateMetrics(lastKnown)
                updateFixAge()
            }
        } catch (ex: SecurityException) {
            // Permission is checked before calling this method.
        }
    }

    private fun stopLocationUpdates() {
        try {
            locationManager.removeUpdates(this)
        } catch (_: SecurityException) {
        }
    }

    override fun onLocationChanged(location: Location) {
        previousLocation = lastLocation
        lastLocation = location
        lastFixTimestamp = System.currentTimeMillis()

        updateHeading()
        updateMetrics(location)
        updateFixAge()
    }

    private fun updateHeading() {
        val current = lastLocation ?: return
        val previous = previousLocation
        val bearing = when {
            previous != null -> previous.bearingTo(current)
            current.hasBearing() -> current.bearing
            else -> currentHeading
        }
        val heading = (bearing + 360f) % 360f
        currentHeading = heading
        val headingInt = heading.toInt()
        binding.directionValue.text = getString(R.string.direction_value_format, headingInt)
        binding.compassView.setHeading(heading)
    }

    private fun updateMetrics(location: Location?) {
        if (location == null) {
            binding.altitudeValue.text = getString(R.string.value_placeholder)
            binding.speedValue.text = getString(R.string.value_placeholder)
            return
        }
        val altitudeUnit = sharedPreferences.getString(PREF_ALTITUDE_UNIT, DEFAULT_ALTITUDE_UNIT) ?: DEFAULT_ALTITUDE_UNIT
        val speedUnit = sharedPreferences.getString(PREF_SPEED_UNIT, DEFAULT_SPEED_UNIT) ?: DEFAULT_SPEED_UNIT

        binding.altitudeValue.text = UnitFormatter.formatAltitude(this, location.altitude, altitudeUnit)
        binding.speedValue.text = UnitFormatter.formatSpeed(this, location.speed, speedUnit)
    }

    private fun updateFixAge() {
        if (lastFixTimestamp == 0L) {
            binding.lastFixValue.text = getString(R.string.last_fix_unknown)
            return
        }
        val elapsedMillis = System.currentTimeMillis() - lastFixTimestamp
        val safeElapsed = if (elapsedMillis < 0) 0L else elapsedMillis
        val seconds = TimeUnit.MILLISECONDS.toSeconds(safeElapsed).coerceAtMost(MAX_FIX_AGE_SECONDS)
        binding.lastFixValue.text = getString(R.string.last_fix_value_format, seconds)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == PREF_ALTITUDE_UNIT || key == PREF_SPEED_UNIT) {
            updateMetrics(lastLocation)
        } else if (key == SettingsActivity.LANGUAGE_KEY) {
            val language = sharedPreferences.getString(SettingsActivity.LANGUAGE_KEY, PREF_LANGUAGE_SYSTEM) ?: PREF_LANGUAGE_SYSTEM
            LanguageUtils.applyLanguagePreference(language)
        }
    }

    companion object {
        private const val MIN_TIME_BW_UPDATES = 1000L
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 0f
        private const val UPDATE_INTERVAL_MS = 1000L
        private const val MAX_FIX_AGE_SECONDS = 9999L

        const val PREF_ALTITUDE_UNIT = "altitude_unit"
        const val PREF_SPEED_UNIT = "speed_unit"
        const val DEFAULT_ALTITUDE_UNIT = "meters"
        const val DEFAULT_SPEED_UNIT = "kmh"
        const val PREF_LANGUAGE_SYSTEM = "system"
    }
}
