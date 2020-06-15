package com.example.rhodiumproject

import androidx.lifecycle.LiveData

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class CellRepository(private val cellDao : cellDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allWords: LiveData<List<Cell>> = cellDao.getAllCellInfo()

    suspend fun insert(cell: Cell) {
        cellDao.insert(cell)
    }
}