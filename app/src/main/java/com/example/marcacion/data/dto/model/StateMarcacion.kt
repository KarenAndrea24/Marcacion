package com.example.marcacion.data.dto.model

import com.example.marcacion.data.dto.response.MarcacionResponse

sealed class StateMarcacion {
    data class Success(val info: MarcacionResponse) : StateMarcacion()
    data class Error(val message: String) : StateMarcacion()
    data object Loading : StateMarcacion()
}