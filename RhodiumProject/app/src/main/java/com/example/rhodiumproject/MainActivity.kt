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
    protected val REQUEST_CHECK_SETTINGS = 0x1
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
            val intent = Intent(this, mapActivity::class.java)
            startActivity(intent)
        }
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                LTESignalStrength()
                mainHandler.postDelayed(this, 5000)
            }
        })
    }

    private fun LTESignalStrength (){

        //Set signal strength and quality to 0
        var servingCellSignalStrength = 0
        var servingCellSignalQuality = 0
        var servingCellSignalnoise = 0
        var neighborCellSignalStrength = 0
        var neighborCellSignalQuality = 0
        var neighborCellSignalnoise = 0
        var servingCellTAC = 0
        var servingCellLAC = 0
        var servingCellPLMN = ""
        var servingCellRAC = 0
        var servingCellId = 0

        //Set an instance of telephony manager
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        //Check location permission
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }

        //Check type of network and assign parameters to signal strength and quality
        val cellInfoList = tm.allCellInfo
        //Put location check high priority to check
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        //Build a location request
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        //Build a listener to check whether the location is on or not
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            for (cellInfo in cellInfoList) {
                if (cellInfo.isRegistered) {
                    if (cellInfo is CellInfoLte) {
                        servingCellSignalStrength = cellInfo.cellSignalStrength.rsrp
                        servingCellSignalQuality = cellInfo.cellSignalStrength.rsrq
                        servingCellSignalnoise = cellInfo.cellSignalStrength.rssnr
                        servingCellTAC =  cellInfo.cellIdentity.tac
                        servingCellPLMN = tm.networkOperator
                        servingCellId =  cellInfo.cellIdentity.ci
                    }
                    else if (cellInfo is CellInfoWcdma && servingCellSignalStrength == 0) {
                        val b = cellInfo.cellSignalStrength.dbm
                        servingCellSignalStrength = b
                        servingCellLAC = cellInfo.cellIdentity.lac
                        servingCellPLMN = tm.networkOperator
                        servingCellId = cellInfo.cellIdentity.cid
                    } else if (cellInfo is CellInfoGsm && servingCellSignalStrength == 0) {
                        val gsm = cellInfo.cellSignalStrength
                        servingCellSignalStrength = gsm.dbm
                        servingCellPLMN = tm.networkOperator
                        servingCellLAC =  cellInfo.cellIdentity.lac
                        servingCellId = cellInfo.cellIdentity.cid
                    }

                }else{
                    if (cellInfo is CellInfoLte) {
                        neighborCellSignalStrength = cellInfo.cellSignalStrength.rsrp
                        neighborCellSignalQuality = cellInfo.cellSignalStrength.rsrq
                        neighborCellSignalnoise = cellInfo.cellSignalStrength.rssnr
                    } else if (cellInfo is CellInfoWcdma && neighborCellSignalStrength == 0) {
                        val b = cellInfo.cellSignalStrength.dbm
                        neighborCellSignalStrength = b
                    } else if (cellInfo is CellInfoGsm && neighborCellSignalStrength == 0) {
                        val gsm = cellInfo.cellSignalStrength
                        neighborCellSignalStrength = gsm.dbm
                    }
                }
            }
        }

        //Build a request to turn on the location
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                try {
                    exception.startResolutionForResult(this@MainActivity,
                        REQUEST_CHECK_SETTINGS)
                }
                catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }
}
