package com.hikarisakamoto.watchful

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson

class GeofenceRepository(private val context: Context) {
    val RADIUS = 10f;
    val EXPIRATION = 1_000_000;

    companion object {
        private const val PREFS_NAME = "GeofenceRepository"
        private const val WATCHED = "WATCHED"
    }

    private val gson = Gson()
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val geofencingClient = LocationServices.getGeofencingClient(context)
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun add(
        watched: Watched,
        success: () -> Unit,
        failure: (error: String) -> Unit
    ) {
        val geofence = buildGeofence(watched)
        if (geofence != null && ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient
                .addGeofences(buildGeofencingRequest(geofence), geofencePendingIntent)
                .addOnSuccessListener {
                    saveAll(getAll() + watched)
                    success()
                }
                .addOnFailureListener {
                    failure(GeofenceErrorMessages.getErrorString(context, it))
                }
        }
    }

    private fun buildGeofence(watched: Watched): Geofence? {
        val latLng = watched.latLng!!

        return Geofence.Builder()
            .setRequestId(watched.fullName)
            .setCircularRegion(
                latLng.latitude,
                latLng.longitude,
                RADIUS
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
            .setExpirationDuration(EXPIRATION.toLong())
            .build()
    }

    private fun buildGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(0)
            .addGeofences(listOf(geofence))
            .build()
    }

    private fun saveAll(list: List<Watched>) {
        preferences
            .edit()
            .putString(WATCHED, gson.toJson(list))
            .apply()
    }

    fun get(fullName: String?) = getAll().firstOrNull { it.fullName == fullName }


    fun getAll(): List<Watched> {
        if (preferences.contains(WATCHED)) {
            val remindersString = preferences.getString(WATCHED, null)
            val arrayOfReminders = gson.fromJson(
                remindersString,
                Array<Watched>::class.java
            )
            if (arrayOfReminders != null) {
                return arrayOfReminders.toList()
            }
        }
        return listOf()
    }
}