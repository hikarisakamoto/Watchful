package com.hikarisakamoto.watchful

class Watched(
    val fullName: String,
    val latitude: Double,
    val longitude: Double
) {
    constructor() : this("", 0.0, 0.0)
}