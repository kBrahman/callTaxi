package zig.zak.taxor.manager

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Handler
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener
import zig.zak.taxor.model.Taxist
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@SuppressLint("MissingPermission")
class LocationManager @Inject constructor(private val ctx: Context, private val apiManager: ApiManager) {

    companion object {
        private val TAG = LocationManager::class.java.simpleName
    }

    var lastKnownLocation: LatLng? = null
    var taxist: Taxist? = null
    var stopSendingLocation = false

    fun sendLocation() {
        Log.i(TAG, "send location")
        sendCoords()
        Handler().postDelayed({
            if (!stopSendingLocation) sendLocation()
        }, 21000)
    }

    private fun sendCoords() = FusedLocationProviderClient(ctx).lastLocation.addOnSuccessListener {
        val latitude = it?.latitude ?: lastKnownLocation!!.latitude
        val longitude = it?.longitude ?: lastKnownLocation!!.longitude
        apiManager.sendCoords(taxist?.phone, latitude, longitude)
    }.addOnFailureListener {
        it.printStackTrace()
    }

    fun getMyCoords(listener: OnSuccessListener<Location>) {
        FusedLocationProviderClient(ctx).lastLocation.addOnSuccessListener(listener).addOnFailureListener {
            it.printStackTrace()
        }
    }

    fun detect(pickLocation: () -> Unit, init: () -> Unit) {
        FusedLocationProviderClient(ctx).lastLocation.addOnSuccessListener {
            if (it == null) {
                pickLocation()
            } else {
                init()
            }
        }.addOnFailureListener {
            pickLocation()
        }
    }
}