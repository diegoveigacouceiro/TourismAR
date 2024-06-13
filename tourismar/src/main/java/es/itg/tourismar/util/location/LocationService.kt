package es.itg.tourismar.util.location

import android.app.ActivityManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import es.itg.geoar.location.LocationData
import es.itg.tourismar.ui.screens.arscreen.ARSceneViewModel

class LocationService : Service() {
    private val _minDistance: Int = 15
    private val _minAccuracy: Int = 15
    private val _notificationID = 33

    private lateinit var locationHandler: LocationHandler
    private lateinit var notificationHandler: LocationNotificationHandler
    private var targetLocations: ArrayList<LocationData> = arrayListOf()
    private var foundLocations: MutableSet<String> = HashSet()


    override fun onCreate() {
        super.onCreate()
        locationHandler = LocationHandler(this)
        notificationHandler = LocationNotificationHandler(this)
        notificationHandler.createNotificationChannel()

        startForeground(
            _notificationID, notificationHandler.createNotification(
                "Location Service",
                "Service running",
                NotificationCompat.PRIORITY_HIGH
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        targetLocations += intent?.getSerializableExtra("targetLocations") as? ArrayList<LocationData>
            ?: arrayListOf()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    if (location.accuracy < _minAccuracy && isAppInBackground()) {
                        checkNearbyLocations(location)
                    }
                }
            }
        }
        locationHandler.startLocationUpdates(locationCallback)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        locationHandler.stopLocationUpdates()
    }

    private fun isAppInBackground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = applicationContext.packageName
        for (processInfo in runningAppProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && processInfo.processName == packageName) {
                return false
            }
        }
        return true
    }

    private fun checkNearbyLocations(location: Location) {
        for (targetLocation in targetLocations) {
            val distance = location.distanceTo(targetLocation.toLocation())
            if (distance < _minDistance && !foundLocations.contains(targetLocation.id)) {
                foundLocations.add(targetLocation.id)
                val notificationTitle = "Contenido Cercano"
                val notificationContent = "Hay un punto de interés cercano a 10 metros."
                val intent = Intent(applicationContext, ARSceneViewModel::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                val notification = notificationHandler.createNotification(
                    title = notificationTitle,
                    contentText = notificationContent,
                    intent = pendingIntent
                )
                notificationHandler.showNotification(notification)
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}