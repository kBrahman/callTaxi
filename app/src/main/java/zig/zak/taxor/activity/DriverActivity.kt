package zig.zak.taxor.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import kotlinx.android.synthetic.main.driver.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import zig.zak.taxor.R
import zig.zak.taxor.manager.ApiManager
import zig.zak.taxor.manager.LocationManager
import zig.zak.taxor.model.Taxist
import zig.zak.taxor.service.TaxiService
import zig.zak.taxor.util.*

class DriverActivity : AppCompatActivity() {
    companion object {
        private val TAG: String = DriverActivity::class.java.simpleName
    }

    private var taxist: Taxist? = null
    private var locationManager: LocationManager? = null
    private var didExit = false

    private val stopDriverActivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")
        val prefs = getSharedPreferences("$packageName.$TAXOR", Context.MODE_PRIVATE)
        if (getSharedPreferences("$packageName.$TAXOR", Context.MODE_PRIVATE).getBoolean(DID_EXIT, true)) {
            val dialog = AlertDialog.Builder(this)
                    .setView(R.layout.dialog_taxist_data)
                    .setPositiveButton(R.string.start, null)
                    .setNegativeButton(android.R.string.cancel) { _, _ ->
                        didExit = true
                        finish()
                    }
                    .setCancelable(false)
                    .create()
            dialog.setOnShowListener { d ->
                val name = (d as AlertDialog).findViewById<EditText>(R.id.edtName)!!
                val phone = d.findViewById<EditText>(R.id.edtPhone)!!
                name.setText(prefs.getString(ContactsContract.Intents.Insert.NAME, ""))
                phone.setText(prefs.getString(ContactsContract.Intents.Insert.PHONE, ""))
                d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val validated = validated(name, phone)
                    if (validated) {
                        val driverPhone = phone.text.toString()
                        taxist = Taxist(name.text.toString(), driverPhone)
                        send()
                        d.dismiss()
                    }
                }
            }

            dialog.show()
        } else {
            taxist = Taxist(prefs.getString(ContactsContract.Intents.Insert.NAME, ""), prefs.getString(ContactsContract.Intents.Insert.PHONE, ""))
            send()
        }
        stopService(Intent(this, TaxiService::class.java))
        registerReceiver(stopDriverActivityReceiver, IntentFilter(FINISH_TAXI_DRIVER_ACTIVITY))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            send()
        } else {
            finish()
        }
    }

    override fun onStop() {
        Log.i(TAG, "onStop")
        if (!didExit) {
            val intent = Intent(this, TaxiService::class.java)
            intent.putExtra(ContactsContract.Intents.Insert.NAME, taxist?.name)
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, taxist?.phone)
            startService(intent)
        }
        locationManager?.stopSendingLocation = true
        val preferences = getSharedPreferences("$packageName.$TAXOR", Context.MODE_PRIVATE)
        preferences.edit().putBoolean(DID_EXIT, didExit).apply()
        preferences.edit().putString(ContactsContract.Intents.Insert.NAME, taxist?.name).putString(ContactsContract.Intents.Insert.PHONE, taxist?.phone).apply()
        super.onStop()
    }

    override fun onRestart() {
        super.onRestart()
        Log.i(TAG, "onRestart")
        stopService(Intent(this, TaxiService::class.java))
        locationManager?.stopSendingLocation = false
        locationManager?.sendLocation()
    }

    private fun send() {
        val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionsOk(this, permissions)) {
            ApiManager().send(taxist, object : Callback<Boolean> {
                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    if (t.message.toString().contains("Failed to connect to")) {
                        Toast.makeText(this@DriverActivity, R.string.maintenance, LENGTH_LONG).show()
                        didExit = true
                        finish()
                    }
                    t.printStackTrace()
                }

                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    Log.i(TAG, "onResponse")
                    if (response.body()!!) {
                        setTitle(R.string.working)
                        setContentView(R.layout.driver)
                        prgrBar.visibility = View.GONE
                    } else {
                        Toast.makeText(this@DriverActivity, R.string.try_later, Toast.LENGTH_LONG).show()
                    }
                    locationManager = LocationManager(this@DriverActivity, taxist)
                    locationManager?.sendLocation()
                }
            })
        } else {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION_PERMISSION)
        }

    }


    fun remove(v: View) {
        val phone = taxist?.phone
        val name = taxist?.name
        Log.i(TAG, "data=>$name, $phone")
        ApiManager().remove(phone!!)
        didExit = true
        finish()
    }

    private fun validated(vararg args: EditText) = !args.any {
        if (it.text.isBlank()) {
            it.error = getString(R.string.cant_be_empty)
            true
        } else false
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(stopDriverActivityReceiver)
    }
}
