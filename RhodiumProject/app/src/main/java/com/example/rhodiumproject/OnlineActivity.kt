package com.example.rhodiumproject

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.ArrayList
import android.content.Intent
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton



class OnlineActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private val newCellActivityRequestCode = 1
    private lateinit var cellViewModel: CellViewModel
//    private var map: MapView? = null
//    private var mapController: IMapController? = map?.controller
//    private val startPoint: GeoPoint = GeoPoint(35.6892, 51.3890)
//    protected val REQUEST_CHECK_SETTINGS = 0x1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online)
//        val ctx = applicationContext
//        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
//        map = findViewById<MapView>(R.id.map)
//        map?.controller?.setZoom(12)
//        map?.controller?.setCenter(startPoint)
//        map?.setBuiltInZoomControls(true)
//        map?.setMultiTouchControls(true)
//        map?.setTileSource(TileSourceFactory.MAPNIK)
//        var mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(applicationContext), map)
//        mLocationOverlay.enableMyLocation()
//        map?.overlays?.add(mLocationOverlay)
//        mLocationOverlay.enableFollowLocation()
//        mLocationOverlay.isDrawAccuracyEnabled

        val button = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab)
        button.setOnClickListener{
            val intent = Intent(this, ServingCellActivity::class.java)
            startActivity(intent)
        }
        /*val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                LTESignalStrength()
                mainHandler.postDelayed(this, 5000)
            }
        })*/

//        val handler = Handler()
//        handler.postDelayed({
//            map?.controller?.setZoom(18)
//            map?.controller?.setCenter(mLocationOverlay.myLocation)
//        }, 5000)
//        requestPermissionsIfNecessary(
//            arrayOf(
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            )
//        )
//        val mainHandler = Handler(Looper.getMainLooper())
//        mainHandler.post(object : Runnable {
//            override fun run() {
//                LTESignalStrength()
//                mainHandler.postDelayed(this, 5000)
//            }
//        })

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = CellListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        cellViewModel = ViewModelProvider(this).get(CellViewModel::class.java)
        cellViewModel.LTE_allCells.observe(this, androidx.lifecycle.Observer{ cell -> cell?.let { adapter.setCells(it) }} )

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
//        val locationRequest = LocationRequest.create().apply {
//            interval = 10000
//            fastestInterval = 5000
//            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        }
//        //Build a location request
//        val builder = LocationSettingsRequest.Builder()
//            .addLocationRequest(locationRequest)

        //Build a listener to check whether the location is on or not
//        val client: SettingsClient = LocationServices.getSettingsClient(this)
//        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
//        task.addOnSuccessListener { locationSettingsResponse ->
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
//        task.addOnFailureListener { exception ->
//            if (exception is ResolvableApiException){
//                try {
//                    exception.startResolutionForResult(this@OnlineActivity,
//                        REQUEST_CHECK_SETTINGS)
//                }
//                catch (sendEx: IntentSender.SendIntentException) {
//                }
//            }
//        }
    }
//    override fun onResume() {
//        super.onResume()
//        map?.onResume()
//
//    }
//
//    override fun onPause() {
//        super.onPause()
//        map?.onPause()
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String?>,
//        grantResults: IntArray
//    ) {
//        val permissionsToRequest: ArrayList<String?> = ArrayList()
//        for (i in grantResults.indices) {
//            permissionsToRequest.add(permissions[i])
//        }
//        if (permissionsToRequest.size > 0) {
//            ActivityCompat.requestPermissions(
//                this,
//                permissionsToRequest.toArray(arrayOfNulls(0)),
//                REQUEST_PERMISSIONS_REQUEST_CODE
//            )
//        }
//    }
//    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
//        val permissionsToRequest: ArrayList<String> = ArrayList()
//        for (permission in permissions) {
//            if (ContextCompat.checkSelfPermission(this, permission)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                // Permission is not granted
//                permissionsToRequest.add(permission)
//            }
//        }
//        if (permissionsToRequest.size > 0) {
//            ActivityCompat.requestPermissions(
//                this,
//                permissionsToRequest.toArray(arrayOfNulls(0)),
//                REQUEST_PERMISSIONS_REQUEST_CODE
//            )
//        }
//    }
//}