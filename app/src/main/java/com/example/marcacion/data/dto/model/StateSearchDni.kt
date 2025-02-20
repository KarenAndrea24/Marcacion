package com.example.marcacion.data.dto.model

import com.example.marcacion.data.dto.response.SearchDniResponse

sealed class StateSearchDni {
    data class Success(val info: SearchDniResponse) : StateSearchDni()
    data class Error(val message: String) : StateSearchDni()
    data object Loading : StateSearchDni()
}