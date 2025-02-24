package com.example.marcacion.data.dto.response

import com.google.gson.annotations.SerializedName

data class MarcacionResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: Data
)

data class Data(
    @SerializedName("id_user")
    val idUser: String,

    @SerializedName("id_marcacion_user")
    val idMarcacionUser: String,

    @SerializedName("tipo_marcacion")
    val tipoMarcacion: String,

    @SerializedName("fecha_hora")
    val fechaHora: String,

    @SerializedName("latitud")
    val latitud: String,

    @SerializedName("longitud")
    val longitud: String,

    @SerializedName("imagen")
    val imagen: String,

    @SerializedName("ip")
    val ip: String,

    @SerializedName("fecha_editado")
    val fechaEditado: String,

    @SerializedName("fecha_creado")
    val fechaCreado: String,

    @SerializedName("id")
    val id: Int
)