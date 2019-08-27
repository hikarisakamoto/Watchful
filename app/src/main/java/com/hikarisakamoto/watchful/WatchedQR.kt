package com.hikarisakamoto.watchful

import android.content.IntentSender
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.android.synthetic.main.activity_watched_qr.*
import java.text.DateFormat
import java.util.*

class WatchedQR : AppCompatActivity() {

    var watchedKey: String = ""
    var watchedName: String = ""

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private var mLocationRequest: LocationRequest? = null

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private var mLocationSettingsRequest: LocationSettingsRequest? = null

    /**
     * Callback for Location events.
     */
    private var mLocationCallback: LocationCallback? = null

    /**
     * Represents a geographical location.
     */
    private var mCurrentLocation: Location? = null

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

    /**
     * Constant used in the location settings dialog.
     */
    private val REQUEST_CHECK_SETTINGS = 0x1

    // Keys for storing activity state in the Bundle.
    private val KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates"
    private val KEY_LOCATION = "location"
    private val KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string"

    /**
     * Provides access to the Fused Location Provider API.
     */
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    /**
     * Provides access to the Location Settings API.
     */
    private var mSettingsClient: SettingsClient? = null

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private var mRequestingLocationUpdates: Boolean? = null

    // Labels.
    private var mLatitudeLabel: String? = "Latitude"
    private var mLongitudeLabel: String? = "Longitude"
    private var mLastUpdateTimeLabel: String? = "Timestamp"

    /**
     * Time when the location was updated represented as a String.
     */
    private var mLastUpdateTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watched_qr)
        watchedKey = intent.getStringExtra("WatchedKey")
        watchedName = intent.getStringExtra("WatchedName")

        val bitmap = QRCodeHelper
            .newInstance(this)
            .setContent(watchedKey)
            .setErrorCorrectionLevel(ErrorCorrectionLevel.Q)
            .setMargin(2)
            .qrcOde
        imgWatchedQR.setImageBitmap(bitmap)

        // Update values using data stored in the Bundle.
//        updateValuesFromBundle(savedInstanceState)
//
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        mSettingsClient = LocationServices.getSettingsClient(this)
//
//        // Kick off the process of building the LocationCallback, LocationRequest, and
//        // LocationSettingsRequest objects.
//        createLocationCallback()
//        createLocationRequest()
//        buildLocationSettingsRequest()
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                    KEY_REQUESTING_LOCATION_UPDATES
                )
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING)
            }
            updateWatchedLocation()
        }
    }

    /**
     * Creates a callback for receiving location events.
     */
    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                mCurrentLocation = locationResult!!.lastLocation
                mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
                updateWatchedLocation()
            }
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION`. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     *
     *
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     *
     *
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest!!.interval = UPDATE_INTERVAL_IN_MILLISECONDS

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS

        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private fun updateWatchedLocation() {
        if (mCurrentLocation != null) {
            val mLatitude = String.format(
                Locale.ENGLISH, "%s: %f", mLatitudeLabel,
                mCurrentLocation!!.getLatitude()
            )
            val mLongitude = String.format(
                Locale.ENGLISH, "%s: %f", mLongitudeLabel,
                mCurrentLocation!!.getLongitude()
            )
            val mLastUpdateTime = String.format(
                Locale.ENGLISH, "%s: %s",
                mLastUpdateTimeLabel, mLastUpdateTime
            )
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("Watched/$watchedName")
            val watched = Watched(
                watchedName,
                mCurrentLocation!!.latitude,
                mCurrentLocation!!.longitude,

                )
            val firebaseReturnedValue = myRef.push()
            firebaseReturnedValue.setValue(watched)

            Toast.makeText(baseContext, mLatitude, Toast.LENGTH_LONG).show()
            Toast.makeText(baseContext, mLongitude, Toast.LENGTH_LONG).show()
            Toast.makeText(baseContext, mLastUpdateTime, Toast.LENGTH_LONG).show()

            Log.d("LATITUDE: ", mLatitude)
            Log.d("LONGITUDE: ", mLongitude)
            Log.d("TIMESTAMP: ", mLastUpdateTime)
        }
    }

    /**
     * Uses a [com.google.android.gms.location.LocationSettingsRequest.Builder] to build
     * a [com.google.android.gms.location.LocationSettingsRequest] that is used for checking
     * if a device has the needed location settings.
     */
    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        mLocationRequest?.let { builder.addLocationRequest(it) }
        mLocationSettingsRequest = builder.build()
    }


    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private fun startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient?.checkLocationSettings(mLocationSettingsRequest)
            ?.addOnSuccessListener(this) {


                mFusedLocationClient?.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback, Looper.myLooper()
                )

                updateWatchedLocation()

            }?.addOnFailureListener(this) { e ->
                val statusCode = (e as ApiException).statusCode
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                        } catch (sie: IntentSender.SendIntentException) {
                        }

                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        val errorMessage =
                            "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings."
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                        mRequestingLocationUpdates = false
                    }
                }

                updateWatchedLocation()

            }
    }


    public override fun onResume() {
        super.onResume()
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
//        if (this!!.mRequestingLocationUpdates) {
        startLocationUpdates()
//        }

        updateWatchedLocation()
    }
}
