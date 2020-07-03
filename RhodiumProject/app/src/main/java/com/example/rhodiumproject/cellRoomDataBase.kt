package com.example.rhodiumproject

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
/*
@Database(entities = arrayOf(LTE_Cell::class, UMTS_Cell::class, GSM_Cell::class), version = 1, exportSchema = false)
public abstract class CellRoomDatabase : RoomDatabase() {
    abstract fun LTECellDao(): LTECellDao
    abstract fun UMTSCellDao() : UMTSCellDao
    abstract fun GSMCellDao() : GSMCellDao
    private class CellDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    //var wordDao = database.wordDao()
                    populateDatabase(database.LTECellDao())
                }
            }
        }
        suspend fun populateDatabase(LTEDao: LTECellDao) {
            // Delete all content here.
            LTEDao.deleteAll()
            // Add sample words.
            var LTECell = LTE_Cell(11111, "11111", "11111", "11111", "11111", "11111")
            LTEDao.insert(LTECell)
            LTECell = LTE_Cell(22222, "22222", "22222", "22222", "22222", "22222")
            LTEDao.insert(LTECell)
        }
        companion object {
            // Singleton prevents multiple instances of database opening at the
            // same time.
            @Volatile
            private var INSTANCE: CellRoomDatabase? = null
            fun getDatabase(context: Context, scope: CoroutineScope): CellRoomDatabase {
                val tempInstance = INSTANCE
                if (tempInstance != null) {
                    return tempInstance
                }
                synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        CellRoomDatabase::class.java,
                        "cell_database"
                    ).addCallback(CellDatabaseCallback(scope)).build()
                    INSTANCE = instance
                    return instance
                }
            }
        }
    }
}
*/
@Database(entities = arrayOf(LTE_Cell::class), version = 1, exportSchema = false)
abstract class CellRoomDatabase : RoomDatabase() {

    abstract fun LTECellDao(): LTECellDao

    private class CellDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    var LTECellDao = database.LTECellDao()

                    // Delete all content here.
                    LTECellDao.deleteAll()

                    // Add sample words.
                    var LTE_Cell= LTE_Cell(1, "1", "1", "1", "1", "1", "1")
                    LTECellDao.insert(LTE_Cell)
                    LTE_Cell = LTE_Cell(2, "2", "2", "2", "2", "2", "2")
                    LTECellDao.insert(LTE_Cell)

                    // TODO: Add your own words!
                    LTE_Cell = LTE_Cell(3, "3", "3", "3", "3", "3", "3")
                    LTECellDao.insert(LTE_Cell)
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: CellRoomDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): CellRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CellRoomDatabase::class.java,
                    "word_database"
                )
                    .addCallback(CellDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}