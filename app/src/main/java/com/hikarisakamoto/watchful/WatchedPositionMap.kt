package com.hikarisakamoto.watchful

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class WatchedPositionMap : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var myRef: DatabaseReference
    var watchedKey: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watched_position_map)
        watchedKey = intent.getStringExtra("WatchedKey")
        val database = FirebaseDatabase.getInstance()
        myRef = database.getReference("Watched/$watchedKey")
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val watched = dataSnapshot.getValue(Watched::class.java)
                val latLng = LatLng(watched!!.latitude, watched.longitude)
                mMap.addMarker(MarkerOptions().position(latLng).title(watched.fullName))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Toast.makeText(
                    baseContext,
                    "Failed to read value: " + error.toException(),
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val latLng = LatLng(0.0, 0.0)
        mMap.addMarker(MarkerOptions().position(latLng).title("Fulano"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))

    }
}
