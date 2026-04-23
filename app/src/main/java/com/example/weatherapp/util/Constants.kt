package com.example.weatherapp.util

object Constants {
    /** Cache TTL: 30 minutes before a new network fetch is forced. */
    const val CACHE_TTL_MS = 30 * 60 * 1000L

    /** Minimum location accuracy in metres before accepting a GPS fix. */
    const val MIN_LOCATION_ACCURACY_M = 100f

    /** DataStore preference keys */
    const val PREF_TEMPERATURE_UNIT = "pref_temperature_unit"
    const val PREF_WIND_UNIT = "pref_wind_unit"

    /** Temperature unit values stored in DataStore */
    const val UNIT_CELSIUS = "celsius"
    const val UNIT_FAHRENHEIT = "fahrenheit"

    /** Wind unit values */
    const val UNIT_MS = "m/s"
    const val UNIT_MPH = "mph"
    const val UNIT_KMH = "km/h"
}
