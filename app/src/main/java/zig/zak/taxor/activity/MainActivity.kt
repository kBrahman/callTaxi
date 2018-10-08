package zig.zak.taxor.activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import zig.zak.taxor.R
import zig.zak.taxor.util.TAXOR

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
        private const val TAXI_DRIVER = "taxi_driver"
        private const val WHO = "who"
        private const val PASSENGER = "passenger"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isNetworkConnected()) {
            setTitle(R.string.network_error)
            setContentView(R.layout.activity_no_net)
        } else {
            init()
        }

    }

    fun refresh(v: View) {
        if (isNetworkConnected()) init()
    }

    private fun init() {
        Log.i(TAG, "init")
        val sharedPreferences = getSharedPreferences("$packageName.$TAXOR", Context.MODE_PRIVATE)
        val who = sharedPreferences.getString(WHO, null)
        when (who) {
            null -> AlertDialog.Builder(this).setMessage(R.string.who_are_you)
                    .setPositiveButton(R.string.taxi_driver) { _, _ ->
                        startActivity(Intent(this, DriverActivity::class.java))
                        sharedPreferences.edit().putString(WHO, TAXI_DRIVER).apply()
                    }.setNegativeButton(R.string.passenger) { _, _ ->
                        Log.i(TAG,"start passenger Activity")
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
