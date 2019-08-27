package com.hikarisakamoto.watchful

import com.google.android.gms.location.Geofence

class Watched(
    val fullName: String,
    val latitude: Double,
    val longitude: Double,
    geofence: Geofence
) {

}