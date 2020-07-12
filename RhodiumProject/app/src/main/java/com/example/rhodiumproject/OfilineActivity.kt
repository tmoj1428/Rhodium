package com.example.rhodiumproject

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.ArrayList

class OfilineActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private var map: MapView? = null
    private var db:CellRoomDatabase? = null
    private val startPoint: GeoPoint = GeoPoint(35.6892, 51.3890)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        map = findViewById<MapView>(R.id.map)
        map?.controller?.setZoom(12)
        map?.controller?.setCenter(startPoint)
        map?.setBuiltInZoomControls(true)
        map?.setMultiTouchControls(true)
        map?.setTileSource(TileSourceFactory.MAPNIK)
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
                pointer()
                mainHandler.postDelayed(this, 5000)
            }
        })
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
            }
        }
    }
}