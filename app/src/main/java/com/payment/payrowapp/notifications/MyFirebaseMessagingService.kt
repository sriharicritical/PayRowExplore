package com.payment.payrowapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.payment.payrowapp.R
import com.payment.payrowapp.generateqrcode.QRCodeConfirmationActivity


class MyFirebaseMessagingService : FirebaseMessagingService() {

   /* override fun onNewToken(s: String) {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(object : OnCompleteListener<InstanceIdResult?> {
                override fun onComplete(task: Task<InstanceIdResult?>) {
                    if (!task.isSuccessful) {
                        return
                    }

                    // Get new Instance ID token
                    val token: String = task.getResult()!!.getToken()
                }
            })
    }*/

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.v("onMessage", "onMessage" + remoteMessage.data)
        sendNotification(remoteMessage.getNotification()!!.getBody()!!, remoteMessage.data)
    }


    private fun sendNotification(messageBody: String, data: MutableMap<String, String>) {
        val intent = Intent(this, QRCodeConfirmationActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        intent.putExtra("status", data.get("status"))
        intent.putExtra("Amount", data.get("amount"))
        intent.putExtra("type", data.get("type"))
        intent.putExtra("orderNumber", data.get("orderNumber"))
        intent.putExtra("channel","Paybylink")
        val notification_id = System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getActivity(
            this, notification_id /* Request code */, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val channelId = "fcm_default_channel"
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.payrow_icon)
                .setContentTitle("PayRow Notification")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(
            notification_id /* ID of notification */,
            notificationBuilder.build()
        )
    }


}
