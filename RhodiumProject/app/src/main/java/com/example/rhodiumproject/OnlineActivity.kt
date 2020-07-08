package com.example.rhodiumproject

//import androidx.lifecycle.observe
import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.PathOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*


class OnlineActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    //    private val newCellActivityRequestCode = 1
//    private lateinit var cellViewModel: CellViewModel
    private var cellViewModel: CellViewModel? = null
    private var map: MapView? = null
    private var mapController: IMapController? = map?.controller
    private val startPoint: GeoPoint = GeoPoint(35.6892, 51.3890)
    protected val REQUEST_CHECK_SETTINGS = 0x1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online)
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        map = findViewById<MapView>(R.id.map)
        map?.controller?.setZoom(12)
        map?.controller?.setCenter(startPoint)
        map?.setBuiltInZoomControls(true)
        map?.setMultiTouchControls(true)
        map?.setTileSource(TileSourceFactory.MAPNIK)
        var mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(applicationContext), map)
        mLocationOverlay.enableMyLocation()
        map?.overlays?.add(mLocationOverlay)
        mLocationOverlay.enableFollowLocation()
        mLocationOverlay.isDrawAccuracyEnabled

        val lon = mLocationOverlay.myLocation.altitude
        val lat = mLocationOverlay.myLocation.longitude
        val startPoint = GeoPoint(35.6892, 51.3890)
        val startMarker = Marker(map)
        startMarker.setPosition(startPoint)
        map?.overlays?.add(startMarker)


        val handler = Handler()
        handler.postDelayed({
            map?.controller?.setZoom(18)
            map?.controller?.setCenter(mLocationOverlay.myLocation)
        }, 5000)
        requestPermissionsIfNecessary(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                LTESignalStrength()
                mainHandler.postDelayed(this, 5000)
            }
        })

//        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
//        val adapter = CellListAdapter(this)
//        recyclerView.adapter = adapter
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        cellViewModel = ViewModelProvider(this).get(CellViewModel::class.java)
//        cellViewModel.LTE_allCells.observe(this, androidx.lifecycle.Observer{ cell -> cell?.let { adapter.setCells(it) }} )
//
//        val fab = findViewById<FloatingActionButton>(R.id.fab)
//        fab.setOnClickListener {
//            val intent = Intent(this@OnlineActivity, NewCellActivity::class.java)
//            startActivityForResult(intent, newCellActivityRequestCode)
//        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == newCellActivityRequestCode && resultCode == Activity.RESULT_OK) {
//            data?.getStringArrayExtra(NewCellActivity.EXTRA_REPLY)?.let {
//                val LTEcell = LTE_Cell(it[0].toInt(), it[1], it[2], it[3], it[4], it[5], it[6])
//
//                cellViewModel.LTEinsert(LTEcell)
//            }
//        }
//        else {
//            Toast.makeText(
//                applicationContext,
//                "",
//                Toast.LENGTH_LONG
//            ).show()
//        }
//    }

    private fun LTESignalStrength (){
        //Connect text and text2 to text views
       /* val cellIDView = findViewById<TextView>(R.id.cell_id)
        val RSRPView = findViewById<TextView>(R.id.RSRP)
        val RSRPView_N = findViewById<TextView>(R.id.RSRP_N)
        val RSRQView = findViewById<TextView>(R.id.RSRQ)
        val RSRQView_N = findViewById<TextView>(R.id.RSRQ_N)
        val PLMNView = findViewById<TextView>(R.id.PLMN)
        val TACView = findViewById<TextView>(R.id.TAC)
        val CINRView = findViewById<TextView>(R.id.CINR)*/
        //Set signal strength and quality to 0
        var mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(applicationContext), map)
        mLocationOverlay.enableMyLocation()
        val lon = mLocationOverlay.myLocation.altitude
        val lat = mLocationOverlay.myLocation.longitude
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
            /*RSRPView.text = "Serving Cell Strength(RSRP) : " + RSRP.toString()
            RSRPView_N.text =  "Neighbor Cell Strength(RSRP) : " + RSRP_neighbor.toString()
            RSRQView.text =  "Serving Cell Quality(RSRQ) : " + RSRQ.toString()
            RSRQView_N.text =  "Neighbor Cell Quality(RSRQ) : " + RSRQ_neighbor.toString()
            cellIDView.text = "Serving Cell ID : " + servingCellId.toString()
            PLMNView.text = "Serving Cell PLMN : " + servingCellPLMN
            TACView.text = "Serving Cell TAC : " + servingCellTAC.toString()
            CINRView.text = "Serving Cell CINR : " + servingCellSignalnoise.toString()*/
            val info = LTE_Cell(cellId = servingCellId.toString(), RSRP = RSRP.toString(), RSRQ = RSRQ.toString(), CINR = servingCellSignalnoise.toString(), TAC = servingCellTAC.toString(), PLMN = servingCellPLMN, altitude = lon, longtitude = lat)
            //db?.LTECellDao()?.insert(info)
            cellViewModel?.LTEinsert(info)
        }

        //Build a request to turn on the location
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                try {
                    exception.startResolutionForResult(this@OnlineActivity,
                        REQUEST_CHECK_SETTINGS)
                }
                catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        map?.onResume()

    }

    override fun onPause() {
        super.onPause()
        map?.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        val permissionsToRequest: ArrayList<String?> = ArrayList()
        for (i in grantResults.indices) {
            permissionsToRequest.add(permissions[i])
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toArray(arrayOfNulls(0)),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }
    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest: ArrayList<String> = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toArray(arrayOfNulls(0)),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }
}