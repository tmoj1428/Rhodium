package com.example.rhodiumproject

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "LTE_table", indices = arrayOf(
    Index(value = ["altitude", "longtitude"],
        unique = true)
))
data class LTE_Cell(
    @PrimaryKey (autoGenerate = true)
    @ColumnInfo(name = "ID")
    var ID: Long?=null,
    var cellId: String?,
    var RSRP: String?,
    var RSRQ: String?,
    var CINR: String?,
    var TAC: String?,
    var PLMN: String?,
    var altitude: Float,
    var longtitude: Float
)

@Entity(tableName = "UMTS_table")
data class UMTS_Cell(
    @PrimaryKey
    var cellId: Int,
    var RSCP: String?,
    var EC_N0: String?,
    var LAC: String?,
    var PLMN: String?
)

@Entity(tableName = "GSM_table")
data class GSM_Cell(
    @PrimaryKey
    var cellId: Int,
    var C1: String?,
    var C2: String?,
    var RxLev: String?,
    var LAC: String?,
    var PLMN: String?
)