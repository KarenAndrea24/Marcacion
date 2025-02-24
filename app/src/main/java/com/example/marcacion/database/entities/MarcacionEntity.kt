package com.example.marcacion.database.entities
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marcaciones")
data class MarcacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dni: String,
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val estado: Int // 0: Pendiente, 1: Sincronizado
)