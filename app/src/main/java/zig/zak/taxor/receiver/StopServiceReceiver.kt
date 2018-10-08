package zig.zak.taxor.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract.Intents.Insert.PHONE
import android.util.Log
import zig.zak.taxor.manager.ApiManager
import zig.zak.taxor.service.TaxiService
import zig.zak.taxor.util.DID_EXIT
import zig.zak.taxor.util.FINISH_TAXI_DRIVER_ACTIVITY
import zig.zak.taxor.util.TAXOR


class StopServiceReceiver : BroadcastReceiver() {
    companion object {
        private val TAG = StopServiceReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.i(TAG, "onReceive")
        val preferences = context.getSharedPreferences("${context.packageName}.$TAXOR", Context.MODE_PRIVATE)
        val phone = preferences?.getString(PHONE, "")
        if (phone?.isNotEmpty()!!) {
            ApiManager().remove(phone)
        }
        context.stopService(Intent(context, TaxiService::class.java))
        preferences.edit().putBoolean(DID_EXIT, true).apply()
        context.sendBroadcast(Intent(FINISH_TAXI_DRIVER_ACTIVITY))
    }

}