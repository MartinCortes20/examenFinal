package com.escom.examenfinal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.escom.examenfinal.data.model.LocationRecord

@Database(entities = [LocationRecord::class], version = 1, exportSchema = false)
abstract class LocationDatabase : RoomDatabase() {
    
    abstract fun locationDao(): LocationDao
    
    companion object {
        @Volatile
        private var INSTANCE: LocationDatabase? = null
        
        fun getDatabase(context: Context): LocationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocationDatabase::class.java,
                    "location_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
