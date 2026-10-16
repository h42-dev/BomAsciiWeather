package com.example.asciiweather.data
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

data class DetectedLocation(val name: String, val latitude: Double, val longitude: Double)

class LocationHelper(private val context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)
    @SuppressLint("MissingPermission")
    suspend fun detectCurrentLocation(): DetectedLocation? = withContext(Dispatchers.IO) {
        try {
            val loc = client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token).await()
                ?: client.lastLocation.await() ?: return@withContext null
            val lat = (loc.latitude * 10000).toInt() / 10000.0
            val lon = (loc.longitude * 10000).toInt() / 10000.0
            val name = try {
                @Suppress("DEPRECATION")
                val a = Geocoder(context, Locale.getDefault()).getFromLocation(lat, lon, 1)?.firstOrNull()
                "${(a?.locality ?: a?.subAdminArea ?: "GPS").uppercase()}, ${(a?.adminArea?.take(3) ?: "AU").uppercase()}"
            } catch (e: Exception) { "GPS ($lat, $lon)" }
            DetectedLocation(name, lat, lon)
        } catch (e: Exception) { null }
    }
}
