package com.example.marcacion.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.marcacion.database.entities.MarcacionEntity

@Dao
interface MarcacionDao {

    // CREATE: Insertar una nueva marcación.
    @Insert
    suspend fun insertarMarcacion(marcacion: MarcacionEntity): Long

    // READ: Obtener todas las marcaciones.
    @Query("SELECT * FROM marcaciones")
    suspend fun obtenerTodas(): List<MarcacionEntity>

    // READ: Obtener una marcación por ID.
    @Query("SELECT * FROM marcaciones WHERE id = :id")
    suspend fun obtenerPorId(id: Int): MarcacionEntity?

    // UPDATE: Actualizar el estado de una marcación a 1 (Sincronizado).
    @Query("UPDATE marcaciones SET estado = 1 WHERE id = :id")
    suspend fun actualizarEstado(id: Int): Int
    // El método retorna un Int que indica cuántas filas se han actualizado.

    // DELETE: Eliminar una marcación por ID.
    @Query("DELETE FROM marcaciones WHERE id = :id")
    suspend fun eliminarMarcacion(id: Int): Int
    // Este método retorna el número de filas eliminadas.

    @Query("UPDATE marcaciones SET estado = 1 WHERE id = (SELECT id FROM marcaciones ORDER BY id DESC LIMIT 1) AND estado = 0")
    fun updateEstadoMarcacion()
    // Este método actualizamos el ultimo registro y si esta en 0 a 1.

    @Query("SELECT * FROM marcaciones WHERE estado = 0 ORDER BY id DESC LIMIT 1")
    suspend fun obtenerPendientes(): List<MarcacionEntity>
}
