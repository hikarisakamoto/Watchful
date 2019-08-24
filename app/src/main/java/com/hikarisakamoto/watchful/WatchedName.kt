package com.hikarisakamoto.watchful

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_watched_name.*


class WatchedName : AppCompatActivity() {

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
                    val mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    val database = FirebaseDatabase.getInstance()
                    val myRef = database.getReference("Watched")
                    val watched = Watched(txtWatcherName.text.toString(), location.latitude, location.longitude)
                    val retorno = myRef.push()
                    retorno.setValue(watched)
                    val intent = Intent(baseContext, WatchedQR::class.java)
                    intent.putExtra("WatchedKey", retorno.key)
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
}
