package com.example.marcacion.data.dto.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("user")
    val user: User
)

data class User(
    @SerializedName("id")
    val id: Int,

    @SerializedName("id_cargo")
    val idCargo: Int,

    @SerializedName("id_labor")
    val idLabor: Int,

    @SerializedName("dni")
    val dni: String,

    @SerializedName("check_digit")
    val checkDigit: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("last_name_father")
    val lastNameFather: String,

    @SerializedName("last_name_mother")
    val lastNameMother: String?,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("address")
    val address: String?,

    @SerializedName("birth_date")
    val birthDate: String,

    @SerializedName("state")
    val state: Int,

    @SerializedName("email_verified_at")
    val emailVerifiedAt: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)