package com.hikarisakamoto.watchful

import com.google.android.gms.location.Geofence
import com.google.android.gms.maps.model.LatLng

class Watched(
    val fullName: String,
    var latLng: LatLng?,
    val longitude: Double,
    geofence: Geofence
) {

}