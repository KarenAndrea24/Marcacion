package com.example.marcacion.data.dto.model

import com.example.marcacion.data.dto.response.LoginResponse
sealed class StateLogin {
    data class Success(val info: LoginResponse) : StateLogin()
    data class Error(val message: String) : StateLogin()
    data object Loading : StateLogin()
}
