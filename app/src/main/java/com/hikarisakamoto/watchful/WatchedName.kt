package com.hikarisakamoto.watchful

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_watched_name.*


class WatchedName : AppCompatActivity() {
    lateinit var geofencingClient: GeofencingClient
    private val GEOFENCE_RADIUS = 10f

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watched_name)




        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), Context.CONTEXT_INCLUDE_CODE
                )
            }
        } else {
            btnSubmit.setOnClickListener { view ->
                if (checkEditText()) {
                    val location = getLastKnownLocation()
                    val database = FirebaseDatabase.getInstance()
                    val myRef = database.getReference("Watched")

                    geofencingClient = LocationServices.getGeofencingClient(this)
                    val geofence = Geofence.Builder()
                        .setRequestId(txtWatcherName.text.toString())
                        .setCircularRegion(
                            location!!.latitude,
                            location.longitude,
                            GEOFENCE_RADIUS
                        )
                        .setExpirationDuration(1_000_000)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build()

                    val watched = Watched(
                        txtWatcherName.text.toString(),
                        location.latitude,
                        location.longitude,
                        geofence
                    )
                    val firebaseReturnedValue = myRef.push()
                    firebaseReturnedValue.setValue(watched)
                    val intent = Intent(baseContext, WatchedQR::class.java)
                    intent.putExtra("WatchedKey", firebaseReturnedValue.key)
                    intent.putExtra("WatchedName", watched.fullName)
                    startActivity(intent)
                }
            }
        }
    }

    private fun checkEditText(): Boolean {
        if (TextUtils.isEmpty(txtWatcherName.text.toString())) {
            Toast.makeText(this, "Full name field cannot be empty!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        val locationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                // Found best last known location: %s", l);
                bestLocation = l
            }
        }
        return bestLocation
    }
}