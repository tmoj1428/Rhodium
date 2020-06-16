package com.example.rhodiumproject

import androidx.lifecycle.LiveData

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class LTECellRepository(private val cellDao : LTECellDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allLTECells: LiveData<List<LTE_Cell>> = cellDao.getAllCellInfo()

    suspend fun insert(cell: LTE_Cell) {
        cellDao.insert(cell)
    }
}
class UMTSCellRepository(private val cellDao : UMTSCellDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allUMTSCells: LiveData<List<UMTS_Cell>> = cellDao.getAllCellInfo()

    suspend fun insert(cell: UMTS_Cell) {
        cellDao.insert(cell)
    }
}
class GSMCellRepository(private val cellDao : GSMCellDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allGSMCells: LiveData<List<GSM_Cell>> = cellDao.getAllCellInfo()

    suspend fun insert(cell: GSM_Cell) {
        cellDao.insert(cell)
    }
}