package com.hikarisakamoto.watchful

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnWatched.setOnClickListener { view ->
            startActivity(Intent(baseContext, WatchedName::class.java))
        }

        btnWatcher.setOnClickListener { view ->
            startActivity(Intent(baseContext, WatcherActivity::class.java))
        }
    }
}
