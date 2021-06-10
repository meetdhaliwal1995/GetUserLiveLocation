package com.interviewcurrentlocation

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi

class ForgroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "ID"
        private const val LOCATION_NOTIFICATION_ID = 111
        var location: String? = null
    }

    var notificaion: NotificationChannel? = null
    var manager: NotificationManager? = null

    override fun onCreate() {
        createNotificationChannel()

        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        location = intent?.getStringExtra("userLocation")
        Log.e("getINtent", location.toString())

        showNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification(): Int {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("User Location")
            .setContentText(location.toString())
            .setSmallIcon(R.drawable.ic_baseline_location_on_24)
            .setContentIntent(pendingIntent)
//            .setTicker()
            .build()

        startForeground(LOCATION_NOTIFICATION_ID, notification)
//        stopSelf()

        return START_NOT_STICKY

    }

    override fun onBind(intent: Intent?): IBinder? {
        location = intent?.getStringExtra("userLocation")
        Log.e("bind", location.toString())
        return null
    }

    override fun onDestroy() {
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            notificaion = NotificationChannel(
                CHANNEL_ID,
                "Forground Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(notificaion!!)

        }
    }



}