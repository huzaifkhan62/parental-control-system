package com.nabiya.parentviewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var liveScreenView: ImageView
    private val databaseRef = FirebaseDatabase.getInstance().getReference("child_device_stream")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pura screen par sirf live video/photo dikhane ke liye ImageView setup
        liveScreenView = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        setContentView(liveScreenView)

        // Firebase se live frames read karne ke liye listener chalu karein
        startLiveStreamListener()
    }

    private fun startLiveStreamListener() {
        databaseRef.child("current_frame").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val base64String = snapshot.getValue(String::class.java)
                if (!base64String.isNullOrEmpty()) {
                    // Base64 text string ko wapas photo (Bitmap) me badlein
                    val decodedImage = decodeBase64ToBitmap(base64String)
                    if (decodedImage != null) {
                        liveScreenView.setImageBitmap(decodedImage)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Stream Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
