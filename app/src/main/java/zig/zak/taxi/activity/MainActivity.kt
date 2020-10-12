package zig.zak.taxi.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import dagger.android.DaggerActivity
import kotlinx.android.synthetic.main.activity_main.*
import zig.zak.taxi.R
import zig.zak.taxi.manager.LocationManager
import zig.zak.taxi.util.REQUEST_CODE_LOCATION_PERMISSION
import zig.zak.taxi.util.TAXOR
import zig.zak.taxi.util.permissionsOk
import javax.inject.Inject

class MainActivity : DaggerActivity() {


    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
        private const val TAXI_DRIVER = "taxi_driver"
        private const val WHO = "who"
        private const val PASSENGER = "activity_passenger"
        private const val REQUEST_CODE_GET_LOCATION = 1
    }

    @Inject
    lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isNetworkConnected()) {
            setTitle(R.string.network_error)
            setContentView(R.layout.activity_no_net)
        } else {
            requestPermission()
        }

    }

    private fun requestPermission() {
        val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionsOk(this, permissions)) {
            detectLocation()
        } else {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION_PERMISSION)
        }
    }

    private fun detectLocation() {
        setContentView(R.layout.activity_main)
        locationManager.detect({
            startActivityForResult(Intent(this, MapActivity::class.java), REQUEST_CODE_GET_LOCATION)
        }, ::init)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            init()
        } else {
            finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            detectLocation()
        } else {
            AlertDialog.Builder(this).setMessage(R.string.need_loc_perm)
                    .setPositiveButton(R.string.grant_permission) { _, _ ->
                        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION_PERMISSION)
                    }.setNegativeButton(R.string.exit) { _, _ ->
                        finish()
                    }.show()
        }
    }

    fun refresh(v: View) {
        if (isNetworkConnected()) {
            requestPermission()
        }
    }

    private fun init() {
        Log.i(TAG, "init")
        layout.visibility = GONE
        val sharedPreferences = getSharedPreferences("$packageName.$TAXOR", Context.MODE_PRIVATE)
        when (sharedPreferences.getString(WHO, null)) {
            null -> AlertDialog.Builder(this).setMessage(R.string.who_are_you)
                    .setPositiveButton(R.string.taxi_driver) { _, _ ->
                        startActivity(Intent(this, DriverActivity::class.java))
                        sharedPreferences.edit().putString(WHO, TAXI_DRIVER).apply()
                    }.setNegativeButton(R.string.passenger) { _, _ ->
                        startActivity(Intent(this, PassengerActivity::class.java))
                        sharedPreferences.edit().putString(WHO, PASSENGER).apply()
                    }
                    .setOnDismissListener { finish() }
                    .setCancelable(false)
                    .show()
            TAXI_DRIVER -> {
                startActivity(Intent(this, DriverActivity::class.java))
                finish()
            }
            else -> {
                startActivity(Intent(this, PassengerActivity::class.java))
                finish()
            }
        }
    }

    private fun isNetworkConnected() = (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo != null

}
