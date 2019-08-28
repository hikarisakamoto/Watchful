package com.hikarisakamoto.watchful

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_watching.*


class Watching : AppCompatActivity() {
    var watchedKey: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watching)
        watchedKey = intent.getStringExtra("WatchedKey")
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Watched/$watchedKey")
        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val watched = dataSnapshot.getValue(Watched::class.java)
                txtWatchedFullName.text = watched!!.fullName
                txtLatitude.text = watched.latitude.toString()
                txtLongitude.text = watched.longitude.toString()
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

        btnGoToMap.setOnClickListener { view ->
            val intent = Intent(baseContext, WatchedPositionMap::class.java)
            intent.putExtra("WatchedKey", watchedKey)
            startActivity(intent)
        }
    }
}
