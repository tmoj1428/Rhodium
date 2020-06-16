package com.example.rhodiumproject
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LTECellDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cell : LTE_Cell)

    @Update
    fun updateUsers(cell : LTE_Cell)

    @Delete
    fun deleteUsers(cell : LTE_Cell)

    @Query("SELECT * from LTE_table ORDER BY cellId ASC")
    fun getAllCellInfo(): LiveData<List<LTE_Cell>>

    @Query("DELETE FROM LTE_table")
    suspend fun deleteAll()
}
@Dao
interface UMTSCellDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cell : UMTS_Cell)

    @Update
    fun updateUsers(cell : UMTS_Cell)

    @Delete
    fun deleteUsers(cell : UMTS_Cell)

    @Query("SELECT * from UMTS_table ORDER BY cellId ASC")
    fun getAllCellInfo(): LiveData<List<UMTS_Cell>>

    @Query("DELETE FROM UMTS_table")
    suspend fun deleteAll()
}
@Dao
interface GSMCellDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cell : GSM_Cell)

    @Update
    fun updateUsers(cell : GSM_Cell)

    @Delete
    fun deleteUsers(cell : GSM_Cell)

    @Query("SELECT * from GSM_table ORDER BY cellId ASC")
    fun getAllCellInfo(): LiveData<List<GSM_Cell>>

    @Query("DELETE FROM GSM_table")
    suspend fun deleteAll()
}