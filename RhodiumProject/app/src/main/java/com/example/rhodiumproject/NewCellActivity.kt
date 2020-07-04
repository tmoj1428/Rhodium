package com.example.rhodiumproject

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task


abstract class NewCellActivity : AppCompatActivity() {
    protected val REQUEST_CHECK_SETTINGS = 0x1
    abstract fun LTECellDao(): LTECellDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_cell)
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                LTESignalStrength()
                mainHandler.postDelayed(this, 5000)
            }
        })
        val id = findViewById<TextView>(R.id.ID)
        val cellIDView = findViewById<TextView>(R.id.cell_id)
        val RSRPView = findViewById<TextView>(R.id.RSRP)
        val RSRQView = findViewById<TextView>(R.id.RSRQ)
        val CINRView = findViewById<TextView>(R.id.CINR)
        val TACView = findViewById<TextView>(R.id.TAC)
        val PLMNView = findViewById<TextView>(R.id.PLMN)
        val button = findViewById<Button>(R.id.save)
        button.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(cellIDView.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                var LTE_Array = Array<String>(7){"NONE"}
                LTE_Array[0] = id.text.toString()
                LTE_Array[1] = cellIDView.text.toString()
                LTE_Array[2] = RSRPView.text.toString()
                LTE_Array[3] = RSRQView.text.toString()
                LTE_Array[4] = CINRView.text.toString()
                LTE_Array[5] = TACView.text.toString()
                LTE_Array[6] = PLMNView.text.toString()
                //var LTE_Cell= LTE_Cell(cellId, RSRP, RSRQ, CINR, TAC, PLMN)

                replyIntent.putExtra(EXTRA_REPLY, LTE_Array)
                setResult(Activity.RESULT_OK, replyIntent)
            }
            finish()
        }
    }
    companion object {
        const val EXTRA_REPLY = "com.example.android.celllistsql.REPLY"
    }
    private fun LTESignalStrength (){
        //Connect text and text2 to text views
        val cellIDView = findViewById<TextView>(R.id.cell_id)
        val RSRPView = findViewById<TextView>(R.id.RSRP)
        val RSRPView_N = findViewById<TextView>(R.id.RSRP_N)
        val RSRQView = findViewById<TextView>(R.id.RSRQ)
        val RSRQView_N = findViewById<TextView>(R.id.RSRQ_N)
        val PLMNView = findViewById<TextView>(R.id.PLMN)
        val TACView = findViewById<TextView>(R.id.TAC)
        val CINRView = findViewById<TextView>(R.id.CINR)

        //Set signal strength and quality to 0
        //var servingCellSignalStrength = 0
        var RSRP = 0
        var RSRQ = 0
        var servingCellSignalnoise = 0
        var RSRP_neighbor = 0
        var RSRQ_neighbor = 0
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
                        RSRP = cellInfo.cellSignalStrength.rsrp
                        RSRQ = cellInfo.cellSignalStrength.rsrq
                        servingCellSignalnoise = cellInfo.cellSignalStrength.rssnr
                        servingCellTAC = cellInfo.cellIdentity.tac
                        servingCellPLMN = tm.networkOperator
                        servingCellId = cellInfo.cellIdentity.ci
                    }
                    else if (cellInfo is CellInfoWcdma && RSRP == 0) {
                        val b = cellInfo.cellSignalStrength.dbm
                        RSRP = b
                        servingCellLAC = cellInfo.cellIdentity.lac
                        servingCellPLMN = tm.networkOperator
                        servingCellId = cellInfo.cellIdentity.cid
                    }
                    else if (cellInfo is CellInfoGsm && RSRP == 0) {
                        val gsm = cellInfo.cellSignalStrength
                        RSRP = gsm.dbm
                        servingCellPLMN = tm.networkOperator
                        servingCellLAC = cellInfo.cellIdentity.lac
                        servingCellId = cellInfo.cellIdentity.cid
                    }
                }
                else {
                    if (cellInfo is CellInfoLte) {
                        RSRP_neighbor = cellInfo.cellSignalStrength.rsrp
                        RSRQ_neighbor = cellInfo.cellSignalStrength.rsrq
                        neighborCellSignalnoise = cellInfo.cellSignalStrength.rssnr
                    } else if (cellInfo is CellInfoWcdma && RSRP_neighbor == 0) {
                        val b = cellInfo.cellSignalStrength.dbm
                        RSRP_neighbor = b
                    } else if (cellInfo is CellInfoGsm && RSRP_neighbor == 0) {
                        val gsm = cellInfo.cellSignalStrength
                        RSRP_neighbor = gsm.dbm
                    }
                }
            }
            RSRPView.text = "Serving Cell Strength(RSRP) : " + RSRP.toString()
            RSRPView_N.text =  "Neighbor Cell Strength(RSRP) : " + RSRP_neighbor.toString()
            RSRQView.text =  "Serving Cell Quality(RSRQ) : " + RSRQ.toString()
            RSRQView_N.text =  "Neighbor Cell Quality(RSRQ) : " + RSRQ_neighbor.toString()
            cellIDView.text = "Serving Cell ID : " + servingCellId.toString()
            PLMNView.text = "Serving Cell PLMN : " + servingCellPLMN
            TACView.text = "Serving Cell TAC : " + servingCellTAC.toString()
            CINRView.text = "Serving Cell CINR : " + servingCellSignalnoise.toString()

        }

        //Build a request to turn on the location
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                try {
                    exception.startResolutionForResult(this@NewCellActivity,
                        REQUEST_CHECK_SETTINGS)
                }
                catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }
}