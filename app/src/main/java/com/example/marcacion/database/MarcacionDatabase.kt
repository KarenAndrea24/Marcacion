package com.example.marcacion.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.marcacion.database.dao.MarcacionDao
import com.example.marcacion.database.entities.MarcacionEntity

@Database(entities = [MarcacionEntity::class], version = 1, exportSchema = false)
abstract class MarcacionDatabase : RoomDatabase() {

    abstract fun marcacionDao(): MarcacionDao

    companion object {
        @Volatile
        private var INSTANCE: MarcacionDatabase? = null

        fun getDatabase(context: Context): MarcacionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarcacionDatabase::class.java,
                    "marcacion_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
