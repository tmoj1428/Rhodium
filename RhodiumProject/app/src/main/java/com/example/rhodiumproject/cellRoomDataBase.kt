package com.example.rhodiumproject
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Cell::class), version = 1, exportSchema = false)
public abstract class CellRoomDatabase : RoomDatabase() {

    abstract fun cellDao(): cellDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: CellRoomDatabase? = null

        fun getDatabase(context: Context): CellRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CellRoomDatabase::class.java,
                    "cell_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}