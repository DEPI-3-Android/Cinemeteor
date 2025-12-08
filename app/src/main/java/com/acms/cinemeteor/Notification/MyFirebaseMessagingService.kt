package com.acms.cinemeteor.Notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.acms.cinemeteor.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth


class MyFirebaseMessagingService : FirebaseMessagingService() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val dataTitle = message.data["notification_title"]
        val dataBody = message.data["notification_body"]

        val finalTitle = dataTitle ?: message.notification?.title ?: "Cinemeteor"
        val finalBody = dataBody ?: message.notification?.body ?: "You have a new notification"

        saveNotificationToFirestore(finalTitle, finalBody)

        if (message.notification != null) {
            showNotification(finalTitle, finalBody)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(title: String, body: String) {
        val channelId = "cinemeteor_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "App Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notifications)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationId = System.currentTimeMillis().toInt()
        NotificationManagerCompat.from(applicationContext)
            .notify(notificationId, builder.build())
    }


    private fun saveNotificationToFirestore(title: String, body: String) {
        val user = FirebaseAuth.getInstance().currentUser


        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val notificationData = hashMapOf(
                "title" to title,
                "message" to body,
                "timestamp" to System.currentTimeMillis(),
                "isRead" to false
            )

            db.collection("users")
                .document(user.uid)
                .collection("Notifications")
                .add(notificationData)
                .addOnSuccessListener {
                    Log.d("FCM_Service", "Notification successfully saved to Firestore.")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM_Service", "Error saving notification: $e")
                }
        }
    }
}
