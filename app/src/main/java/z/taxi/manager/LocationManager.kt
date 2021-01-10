package z.taxi.manager

import android.annotation.SuppressLint
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener
import z.taxi.model.Taxist
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@SuppressLint("MissingPermission")
class LocationManager @Inject constructor(private val locProvider: FusedLocationProviderClient, private val apiManager: ApiManager) {

    companion object {
        private val TAG = LocationManager::class.java.simpleName
    }

    var lastKnownLocation: LatLng? = null
    var taxist: Taxist? = null
    var stopSendingLocation = false

    fun sendLocation() {
        Log.i(TAG, "sendLocation")
        sendCoords()
        Handler(Looper.getMainLooper()).postDelayed({
            if (!stopSendingLocation) sendLocation()
        }, 21000)
    }

    private fun sendCoords() = locProvider.lastLocation.addOnSuccessListener {
        Log.i(TAG, "on success=>$it")
        if (it != null) {
            lastKnownLocation = LatLng(it.latitude, it.longitude)
        }
        val latitude = it?.latitude ?: lastKnownLocation!!.latitude
        val longitude = it?.longitude ?: lastKnownLocation!!.longitude
        taxist?.lat = lastKnownLocation!!.latitude
        taxist?.lon = lastKnownLocation!!.longitude
        apiManager.sendCoords(taxist?.phone, latitude, longitude)
    }.addOnFailureListener {
        it.printStackTrace()
    }

    fun getMyCoords(listener: OnSuccessListener<Location>) {
        locProvider.lastLocation.addOnSuccessListener(listener).addOnFailureListener {
            it.printStackTrace()
        }
    }

    fun detect(pickLocation: () -> Unit, init: () -> Unit) {
        if (lastKnownLocation != null) {
            init()
            return
        }
        locProvider.lastLocation.addOnSuccessListener {
            if (it == null) {
                pickLocation()
            } else {
                Log.i(TAG, "detected loc=>$it")
                init()
            }
        }.addOnFailureListener {
            pickLocation()
        }
    }
}