package com.example.rhodiumproject

import android.Manifest
import android.content.Context
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
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task


class ServingCellActivity : AppCompatActivity() {
    protected val REQUEST_CHECK_SETTINGS = 0x1
    private val newCellActivityRequestCode = 1
    private var cellViewModel: CellViewModel? = null
    private var db:CellRoomDatabase? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_cell)

       // ViewModelProvider.of(this, cellViewModel).get(ServingCellActivity::class.java)
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                LTESignalStrength()
                mainHandler.postDelayed(this, 5000)

            }
        })
        val all = findViewById<Button>(R.id.all)
        all.setOnClickListener {
            val list = db?.LTECellDao()?.AllCell()
            if (list != null) {
                for (cell in list) {
                    //println(cell.PLMN)
                }
            }
            //val intent = Intent(this, OnlineActivity::class.java)
            //startActivity(intent)
            //var cells = cellViewModel?.LTE_allCells
            //cells
        }
        val save = findViewById<Button>(R.id.save)
        save.setOnClickListener {
            //val id = findViewById<TextView>(R.id.ID)
            val cellIDView = findViewById<TextView>(R.id.cell_id)
            val RSRPView = findViewById<TextView>(R.id.RSRP)
            val RSRQView = findViewById<TextView>(R.id.RSRQ)
            val CINRView = findViewById<TextView>(R.id.CINR)
            val TACView = findViewById<TextView>(R.id.TAC)
            val PLMNView = findViewById<TextView>(R.id.PLMN)
            //val info = LTE_Cell(cellId = cellIDView.toString(), RSRP = RSRPView.toString(), RSRQ = RSRQView.toString(), CINR = CINRView.toString(), TAC = TACView.toString(), PLMN = PLMNView.toString())
            //db?.LTECellDao()?.insert(info)
            //cellViewModel?.LTEinsert(info)
        }

    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == newCellActivityRequestCode && resultCode == Activity.RESULT_OK) {
            data?.getStringArrayExtra(NewCellActivity.EXTRA_REPLY)?.let {
                val LTEcell = LTE_Cell(it[0].toInt(), it[1], it[2], it[3], it[4], it[5], it[6])

                cellViewModel.LTEinsert(LTEcell)
            }
        }
        else {
            Toast.makeText(
                applicationContext,
                "",
                Toast.LENGTH_LONG
            ).show()
        }
    }*/

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
            //val info = LTE_Cell(cellId = servingCellId.toString(), RSRP = RSRP.toString(), RSRQ = RSRQ.toString(), CINR = servingCellSignalnoise.toString(), TAC = servingCellTAC.toString(), PLMN = servingCellPLMN)
            //db?.LTECellDao()?.insert(info)
            //cellViewModel?.LTEinsert(info)
        }

        //Build a request to turn on the location
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                try {
                    exception.startResolutionForResult(this@ServingCellActivity,
                        REQUEST_CHECK_SETTINGS)
                }
                catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }
}