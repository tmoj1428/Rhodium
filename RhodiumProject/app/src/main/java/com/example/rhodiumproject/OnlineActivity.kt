package com.example.rhodiumproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import android.text.Layout
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*

class OnlineActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var db:CellRoomDatabase? = null
    private var map: MapView? = null
    private val startPoint: GeoPoint = GeoPoint(35.6892, 51.3890)
    private var lat = 0.0
    private var lon = 0.0
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

        val loc = GpsMyLocationProvider(applicationContext)
        var mLocationOverlay = MyLocationNewOverlay(loc, map)
        mLocationOverlay.enableMyLocation()
        map?.overlays?.add(mLocationOverlay)
        mLocationOverlay.enableFollowLocation()
        mLocationOverlay.isDrawAccuracyEnabled
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

        db = CellRoomDatabase.getDatabase(context = this)
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
                pointer()
                mainHandler.postDelayed(this, 5000)
            }
        })
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun getLastLocation() {
        if (isLocationEnabled()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                task.result
                requestNewLocationData()
            }
        } else {
            Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 9000
        mLocationRequest.fastestInterval = 5000

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            lat = mLastLocation.latitude
            lon = mLastLocation.longitude
        }
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
        var cellType = ""

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
                        cellType = "LTE"
                    }
                    else if (cellInfo is CellInfoWcdma && servingCellSignalStrength == 0) {
                        val b = cellInfo.cellSignalStrength.dbm
                        servingCellSignalStrength = b
                        servingCellLAC = cellInfo.cellIdentity.lac
                        servingCellPLMN = tm.networkOperator
                        servingCellId = cellInfo.cellIdentity.cid
                        cellType = "WCDMA"
                    } else if (cellInfo is CellInfoGsm && servingCellSignalStrength == 0) {
                        val gsm = cellInfo.cellSignalStrength
                        servingCellSignalStrength = gsm.dbm
                        servingCellPLMN = tm.networkOperator
                        servingCellLAC =  cellInfo.cellIdentity.lac
                        servingCellId = cellInfo.cellIdentity.cid
                        cellType = "GSM"
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
                var flag = true
                println("flag1 : " + flag.toString())
                var templat = ((lat * 1000).toInt()).toFloat() / 1000
                var templon = ((lon * 1000).toInt()).toFloat() / 1000
                //println("temp : " + templat.toString())
                val info = LTE_Cell(cellId = servingCellId.toString(), RSRP = servingCellSignalStrength.toString(), RSRQ = servingCellSignalQuality.toString(), CINR = servingCellSignalnoise.toString(), TAC = servingCellTAC.toString(), PLMN = servingCellPLMN, altitude = templat, longtitude = templon, pointer = false, cellType = cellType)
                println("RSRP1 : " + servingCellSignalStrength.toString())
                var list = db?.LTECellDao()?.AllCell()
                if (list != null) {
                    for (cell in list){
                        if(cell.altitude == templat && cell.longtitude == templon){
                            var info1 = LTE_Cell(ID = cell.ID, cellId = servingCellId.toString(), RSRP = servingCellSignalStrength.toString(), RSRQ = servingCellSignalQuality.toString(), CINR = servingCellSignalnoise.toString(), TAC = servingCellTAC.toString(), PLMN = servingCellPLMN, altitude = templat, longtitude = templon, pointer = false, cellType = cellType)
                            db?.LTECellDao()?.updateUsers(info1)
                            flag = false
                            println("flag2 : " + flag.toString())
                            println("RSRP2 : " + cell.RSRP.toString())
                        }
                    }
                }
                if (flag) {
                    db?.LTECellDao()?.insert(info)
                    println("flag4 : " + flag.toString())
                }
            }
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

    private fun pointer()
    {
        val list = db?.LTECellDao()?.AllCell()
        if (list != null) {
            for (cell in list)
            {
                val startPoint = GeoPoint(cell.altitude.toDouble(), cell.longtitude.toDouble())
                val startMarker = Marker(map)
                val text = "Connection Strength : " + cell.RSRP + "\n" + " Connection Quality : " + cell.RSRQ + "\n" + "Network type is : " + cell.cellType
                if (cell.RSRP?.toInt() ?: 0 > -80){
                    startMarker.setPosition(startPoint)
                    startMarker.textLabelBackgroundColor = Color.GREEN
                    startMarker.setTextIcon(text)
                    map?.overlays?.add(startMarker)
                }else if(cell.RSRP?.toInt()!! < -80 && cell.RSRP?.toInt()!! > -90){
                    startMarker.setPosition(startPoint)
                    startMarker.textLabelBackgroundColor = Color.YELLOW
                    startMarker.setTextIcon(text)
                    map?.overlays?.add(startMarker)
                }else{
                    startMarker.setPosition(startPoint)
                    startMarker.textLabelBackgroundColor = Color.RED
                    startMarker.setTextIcon(text)
                    map?.overlays?.add(startMarker)
                }
                val info = LTE_Cell(cellId = cell.cellId, RSRP = cell.RSRP, RSRQ = cell.RSRQ, CINR = cell.CINR, TAC = cell.TAC, PLMN = cell.PLMN, altitude = cell.altitude, longtitude = cell.longtitude, pointer = true, cellType = cell.cellType)
                db?.LTECellDao()?.updateUsers(info)
            }
        }
    }
}
