package com.example.rhodiumproject
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WordViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CellRepository
    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allCells: LiveData<List<Cell>>

    init {
        val cellsDao = CellRoomDatabase.getDatabase(application).cellDao()
        repository = CellRepository(cellsDao)
        allCells = repository.allWords
    }

    fun insert(cell: Cell) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(cell)
    }
}