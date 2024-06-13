package es.itg.tourismar.util.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import es.itg.tourismar.R

import java.util.concurrent.atomic.AtomicInteger


class LocationNotificationHandler (private val context: Context) {

    private val _channelID = "LocationServiceChannel"

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = _channelID
            val channelName = "Location Service Channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance)

            val notificationManager =
                context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(
        title: String,
        contentText: String,
        priority: Int = NotificationCompat.PRIORITY_LOW,
        intent: PendingIntent? = null
    ): Notification {
        val channelId = _channelID

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_torre_foreground)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(priority)
            .setAutoCancel(true)

        intent?.let {
            builder.setContentIntent(it)
        }

        return builder.build()
    }


    fun showNotification(notification: Notification) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(
            NotificationIdGenerator.generateUniqueId(),
            notification)
    }

}

object NotificationIdGenerator {
    private val idGenerator = AtomicInteger(0)

    fun generateUniqueId(): Int {
        return idGenerator.incrementAndGet()
    }
}