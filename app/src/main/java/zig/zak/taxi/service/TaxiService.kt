package zig.zak.taxi.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.android.DaggerService
import zig.zak.taxi.R
import zig.zak.taxi.activity.DriverActivity
import zig.zak.taxi.manager.LocationManager
import zig.zak.taxi.receiver.StopServiceReceiver
import javax.inject.Inject

class TaxiService : DaggerService() {

    companion object {
        private val TAG: String = TaxiService::class.java.simpleName
        private const val ACTION_SNOOZE = "action_snooze"
        private const val NOTIFICATION_ID = 2
        private const val CHANNEL_ID = "Taxor_channel"
    }

    @Inject
    lateinit var manager: LocationManager

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(CHANNEL_ID, "Taxor", IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(mChannel)
        }
        val intent = Intent(this, StopServiceReceiver::class.java).apply {
            action = ACTION_SNOOZE
        }


        val stopPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.working))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setChannelId("Taxor_channel")
                .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, DriverActivity::class.java), 0))
                .addAction(R.drawable.ic_rest_black_24dp, getString(R.string.rest), stopPendingIntent)
                .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStart")
        manager.sendLocation()
        return START_STICKY
    }


    override fun onDestroy() {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(NOTIFICATION_ID)
        manager.stopSendingLocation = true
        super.onDestroy()
    }
}
