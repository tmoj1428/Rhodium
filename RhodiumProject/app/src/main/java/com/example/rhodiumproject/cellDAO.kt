package com.example.rhodiumproject
import androidx.room.*

@Dao
interface cellDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cell : Cell)

    @Update
    fun updateUsers(cell : Cell)

    @Delete
    fun deleteUsers(cell : Cell)

    @Query("SELECT * from LTE_table ORDER BY cellId ASC")
    fun getAllCellInfo(): List<Cell>

    @Query("DELETE FROM LTE_table")
    suspend fun deleteAll()
}