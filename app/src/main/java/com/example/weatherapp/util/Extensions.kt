package com.example.weatherapp.util

import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

// region View helpers

fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

// endregion

// region Fragment helpers

fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), message, duration).show()
}

/** Collect a Flow safely, honouring the Fragment's lifecycle. */
fun <T> Fragment.collectFlow(flow: Flow<T>, action: suspend (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { action(it) }
        }
    }
}

// endregion

// region Temperature conversion

fun Double.celsiusToFahrenheit(): Double = this * 9.0 / 5.0 + 32.0

fun Double.formatTemperature(useFahrenheit: Boolean): String {
    val value = if (useFahrenheit) celsiusToFahrenheit() else this
    return "${value.roundToInt()}°${if (useFahrenheit) "F" else "C"}"
}

// endregion

// region Wind conversion

fun Double.msToMph(): Double = this * 2.23694
fun Double.msToKmh(): Double = this * 3.6

fun Double.formatWindSpeed(unit: String): String = when (unit) {
    Constants.UNIT_MPH -> "${msToMph().roundToInt()} mph"
    Constants.UNIT_KMH -> "${msToKmh().roundToInt()} km/h"
    else               -> "${this.roundToInt()} m/s"
}

// endregion

// region Date/time formatting

fun Long.toFormattedTime(timezoneOffsetSeconds: Int = 0): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return sdf.format(Date((this + timezoneOffsetSeconds) * 1000L))
}

fun Long.toDayOfWeek(timezoneOffsetSeconds: Int = 0): String {
    val sdf = SimpleDateFormat("EEEE", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return sdf.format(Date((this + timezoneOffsetSeconds) * 1000L))
}

fun Long.toShortDay(timezoneOffsetSeconds: Int = 0): String {
    val sdf = SimpleDateFormat("EEE", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return sdf.format(Date((this + timezoneOffsetSeconds) * 1000L))
}

fun Long.toFormattedDate(timezoneOffsetSeconds: Int = 0): String {
    val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return sdf.format(Date((this + timezoneOffsetSeconds) * 1000L))
}

// endregion

// region Wind direction

fun Int.toWindDirection(): String {
    val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    return directions[((this + 22) / 45) % 8]
}

// endregion

// region LifecycleOwner Flow collection

fun <T> LifecycleOwner.collectFlow(flow: Flow<T>, action: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { action(it) }
        }
    }
}

// endregion
