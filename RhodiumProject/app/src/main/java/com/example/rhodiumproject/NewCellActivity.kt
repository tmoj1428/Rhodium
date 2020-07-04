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
}