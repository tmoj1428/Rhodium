package com.example.rhodiumproject
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "LTE_table")
data class Cell(
    @PrimaryKey var cellId: Int,
    var RSRP: String?,
    var RSRQ: String?,
    var CINR: String?
)