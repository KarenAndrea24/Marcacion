package com.example.marcacion.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.marcacion.database.dao.MarcacionDao
import com.example.marcacion.database.dao.UsuarioDao
import com.example.marcacion.database.entities.MarcacionEntity
import com.example.marcacion.database.entities.UsuarioEntity

@Database(entities = [MarcacionEntity::class, UsuarioEntity::class], version = 1, exportSchema = false)
abstract class MarcacionDatabase : RoomDatabase() {

    abstract fun marcacionDao(): MarcacionDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: MarcacionDatabase? = null

        fun getDatabase(context: Context): MarcacionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarcacionDatabase::class.java,
                    "marcacion_database"
                )
//                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
