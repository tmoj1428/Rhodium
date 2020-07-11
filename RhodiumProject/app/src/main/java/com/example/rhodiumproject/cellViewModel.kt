package com.example.rhodiumproject

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CellViewModel(application: Application) : AndroidViewModel(application) {

    private val LTErepository: LTECellRepository
    //private val UMTSrepository: UMTSCellRepository
    //private val GSMrepository: GSMCellRepository
    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val LTE_allCells: LiveData<List<LTE_Cell>>
    //val UMTS_allCells: LiveData<List<UMTS_Cell>>
    //val GSM_allCells: LiveData<List<GSM_Cell>>

    init {
        val LTE_cellsDao = CellRoomDatabase.getDatabase(application).LTECellDao()
        //val UMTS_cellsDao = CellRoomDatabase.getDatabase(application, viewModelScope).UMTSCellDao()
        //val GSM_cellsDao = CellRoomDatabase.getDatabase(application, viewModelScope).GSMCellDao()
        LTErepository = LTECellRepository(LTE_cellsDao)
        //UMTSrepository = UMTSCellRepository(UMTS_cellsDao)
        //GSMrepository = GSMCellRepository(GSM_cellsDao)
        LTE_allCells = LTErepository.allLTECells
        //UMTS_allCells = UMTSrepository.allUMTSCells
        //GSM_allCells = GSMrepository.allGSMCells
    }

    fun LTEinsert(cell: LTE_Cell) = viewModelScope.launch(Dispatchers.IO) {
        LTErepository.insert(cell)
    }

    //fun LTEinsert(cell: UMTS_Cell) = viewModelScope.launch(Dispatchers.IO) {
    //    UMTSrepository.insert(cell)
    //}
    //fun LTEinsert(cell: GSM_Cell) = viewModelScope.launch(Dispatchers.IO) {
    //    GSMrepository.insert(cell)
    //}
}