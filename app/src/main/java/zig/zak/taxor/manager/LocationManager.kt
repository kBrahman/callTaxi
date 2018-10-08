package zig.zak.taxor.manager

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Handler
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.OnSuccessListener
import zig.zak.taxor.model.Taxist

class LocationManager(private val ctx: Context) {

    companion object {
        private val TAG = LocationManager::class.java.simpleName
    }

    private var taxist: Taxist? = null
    var stopSendingLocation = false

    constructor(ctx: Context, taxist: Taxist?) : this(ctx) {
        this.taxist = taxist
    }

    fun sendLocation() {
        sendCoords()
        Handler().postDelayed({
            if (!stopSendingLocation) sendLocation()
        }, 21000)
    }


    @SuppressLint("MissingPermission")
    fun sendCoords() = FusedLocationProviderClient(ctx).lastLocation.addOnSuccessListener {
        val latitude = it.latitude
        val longitude = it.longitude
        Log.i(TAG, "lat=>$latitude, long=>$longitude")
        ApiManager().sendCoords(taxist?.phone, latitude, longitude)
    }.addOnFailureListener {
        it.printStackTrace()
    }

    @SuppressLint("MissingPermission")
    fun getMyCoords(listener: OnSuccessListener<Location>) {
        FusedLocationProviderClient(ctx).lastLocation.addOnSuccessListener(listener).addOnFailureListener {
            it.printStackTrace()
        }
    }


}