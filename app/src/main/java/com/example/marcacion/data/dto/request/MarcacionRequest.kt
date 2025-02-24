package com.example.marcacion.data.dto.request

import com.google.gson.annotations.SerializedName

data class MarcacionRequest(
    @SerializedName("id_user")
    val idUser: String,

    @SerializedName("id_marcacion_user")
    val idMarcacionUser: String,

    @SerializedName("tipo_marcacion")
    val tipoMarcacion: String,

    @SerializedName("fecha_hora")
    val fechaHora: String,

    @SerializedName("foto")
    val foto: String,

    @SerializedName("latitud")
    val latitud: String,

    @SerializedName("longitud")
    val longitud: String
)