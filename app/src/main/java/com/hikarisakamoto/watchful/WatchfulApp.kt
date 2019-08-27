package com.hikarisakamoto.watchful

import android.app.Application

class WatchfulApp : Application() {

    private lateinit var repository: GeofenceRepository

    override fun onCreate() {
        super.onCreate()
        repository = GeofenceRepository(this)
    }

    fun getRepository() = repository
}