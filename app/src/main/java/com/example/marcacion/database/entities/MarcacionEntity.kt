package com.example.marcacion.database.entities
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marcaciones")
data class MarcacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dni: String,
    val idUserMarcado: String, // ID del usuario marcado
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val fechaHora: String, // Fecha y hora de la marcación
    val estado: Int // 0: Pendiente, 1: Sincronizado
)