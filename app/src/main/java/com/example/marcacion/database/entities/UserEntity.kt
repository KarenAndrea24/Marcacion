package com.example.marcacion.database.entities
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dni: String,
    val nombre: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String?,
    val cargo: String,
    val labor: String
)
