package com.hikarisakamoto.watchful

import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    fun getRepository() = (application as WatchfulApp).getRepository()
}