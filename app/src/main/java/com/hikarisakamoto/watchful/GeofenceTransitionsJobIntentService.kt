package com.hikarisakamoto.watchful

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.maps.model.LatLng


class GeofenceTransitionsJobIntentService : JobIntentService() {

    companion object {
        private const val LOG_TAG = "GeoTrIntentService"
        private const val JOB_ID = 573

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        // 1
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        // 2
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceErrorMessages.getErrorString(
                this,
                geofencingEvent.errorCode
            )
//            Log.e(LOG_TAG, errorMessage)
            return
        }
        // 3
        handleEvent(geofencingEvent)
    }

    private fun handleEvent(event: GeofencingEvent) {
        // 1
        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // 2
            val watched = getFirstAlert(event.triggeringGeofences)
            val fullName = watched?.fullName
            val latLng = watched?.latLng
            if (fullName != null && latLng != null) {
                // 3
                sendNotification(this, fullName, latLng)
            }
        }
    }

    private fun sendNotification(
        geofenceTransitionsJobIntentService: GeofenceTransitionsJobIntentService,
        fullName: String,
        latLng: LatLng
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun getFirstAlert(triggeringGeofences: List<Geofence>): Watched? {
        val firstGeofence = triggeringGeofences[0]
        return (application as WatchfulApp).getRepository().get(firstGeofence.requestId)
    }
}