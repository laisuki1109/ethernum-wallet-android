package com.suki.wallet.utility

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.suki.wallet.LaunchActivity
import com.suki.wallet.MyApplication
import com.suki.wallet.R
import timber.log.Timber

class FirebaseMessagingService : FirebaseMessagingService() {

    private val CHANNEL_ID = "CHANNEL_ID"
    private val CHANNEL_NAME = "CHANNEL_NAME"

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Get updated InstanceID token.
        Timber.d("Refreshed token: $token")

        token.let {
            MyApplication.INSTANCE.pushToken = it
            sendRegistrationToServer(it)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Timber.d("From: ${remoteMessage.from}")

        var title: String? = ""
        var body: String? = ""
        var map: Map<String, String>? = null

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Timber.d("Message Notification Body: ${it.body}")

            title = it.title
            body = it.body
        }

        if (remoteMessage.data.isNotEmpty()) {
            Timber.d("Message data payload: ${remoteMessage.data}")

            map = remoteMessage.data
            if (map.containsKey("title")) {
                title = map["title"]
            }
            if (map.containsKey("body")) {
                body = map["body"]
            }
        }

        val intent = Intent(this, LaunchActivity::class.java)
        if (map != null) {
            for (key in map.keys) {
                intent.putExtra(key, map[key])
            }
        }
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setColor(ContextCompat.getColor(baseContext, R.color.colorPrimary))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setAutoCancel(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
        notificationManager.notify((Math.random() * 9999).toInt(), mBuilder.build())
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String) {
        // TODO: Implement this method to send any registration to your app's servers.
    }
}