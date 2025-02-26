package com.example.marcacion.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.marcacion.database.entities.UsuarioEntity

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuarios(usuarios: List<UsuarioEntity>)

    @Query("SELECT * FROM usuarios WHERE dni = :dni LIMIT 1")
    suspend fun obtenerUsuarioPorDni(dni: String): UsuarioEntity?

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int
}