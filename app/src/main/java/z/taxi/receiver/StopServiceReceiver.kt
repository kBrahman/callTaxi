package z.taxi.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract.Intents.Insert.PHONE
import android.util.Log
import z.taxi.manager.ApiManager
import z.taxi.service.TaxiService
import z.taxi.util.FINISH_TAXI_DRIVER_ACTIVITY
import z.taxi.util.TAXOR


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
        context.sendBroadcast(Intent(FINISH_TAXI_DRIVER_ACTIVITY))
    }

}