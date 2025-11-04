package com.example.gpstracker.util

import android.content.Context
import com.example.gpstracker.R

object UnitFormatter {

    fun formatAltitude(context: Context, altitudeMeters: Double, unit: String): String {
        val (value, unitLabelRes) = when (unit) {
            "feet" -> altitudeMeters * METERS_TO_FEET to R.string.unit_feet
            else -> altitudeMeters to R.string.unit_meters
        }
        return context.getString(
            R.string.altitude_value_format,
            value,
            context.getString(unitLabelRes)
        )
    }

    fun formatSpeed(context: Context, speedMetersPerSecond: Float, unit: String): String {
        val (value, unitLabelRes) = when (unit) {
            "mph" -> speedMetersPerSecond * MPS_TO_MPH to R.string.unit_mph
            "mps" -> speedMetersPerSecond.toDouble() to R.string.unit_mps
            "knots" -> speedMetersPerSecond * MPS_TO_KNOTS to R.string.unit_knots
            else -> speedMetersPerSecond * MPS_TO_KMH to R.string.unit_kmh
        }
        return context.getString(
            R.string.speed_value_format,
            value,
            context.getString(unitLabelRes)
        )
    }

    private const val METERS_TO_FEET = 3.28084
    private const val MPS_TO_KMH = 3.6
    private const val MPS_TO_MPH = 2.23694
    private const val MPS_TO_KNOTS = 1.94384
}
