package com.example.weatherapp.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class LocationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Returns the device's current location as a [Result].
     * Caller is responsible for verifying that location permission has been granted
     * before invoking this function.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<Location> =
        suspendCancellableCoroutine { continuation ->
            val cts = CancellationTokenSource()
            continuation.invokeOnCancellation { cts.cancel() }

            fusedClient
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        Timber.d("Location fix: lat=${location.latitude}, lon=${location.longitude}")
                        continuation.resume(Result.success(location))
                    } else {
                        continuation.resume(
                            Result.failure(IllegalStateException("Location unavailable — ensure GPS is enabled"))
                        )
                    }
                }
                .addOnFailureListener { ex ->
                    Timber.e(ex, "Failed to retrieve location")
                    continuation.resumeWithException(ex)
                }
        }
}
