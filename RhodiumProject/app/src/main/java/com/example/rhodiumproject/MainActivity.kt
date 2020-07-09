package com.example.rhodiumproject

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task


class MainActivity : AppCompatActivity() {
//    protected val REQUEST_CHECK_SETTINGS = 0x1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Remove project title
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }

        //Change activity button to map activity
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val intent = Intent(this, OnlineActivity::class.java)
            startActivity(intent)
        }

        val oflineButton = findViewById<Button>(R.id.Oflinebutton)
        oflineButton.setOnClickListener {
            val intent = Intent(this, OfilineActivity::class.java)
            startActivity(intent)
        }
//        val mainHandler = Handler(Looper.getMainLooper())
//        mainHandler.post(object : Runnable {
//            override fun run() {
//                LTESignalStrength()
//                mainHandler.postDelayed(this, 5000)
//            }
//        })
    }
}
