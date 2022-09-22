package com.udacity.project4.utils

import java.util.concurrent.TimeUnit

object GeofencingConstants{
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)
    const val GEOFENCE_RADIUS_IN_METERS = 100f

}