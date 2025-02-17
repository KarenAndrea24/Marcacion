package com.example.marcacion.data.repository

import com.example.marcacion.data.dto.request.LoginRequest
import com.example.marcacion.data.dto.response.LoginResponse
import com.example.marcacion.data.service.UserServiceImp
import retrofit2.Response

class UserRepository(private val service: UserServiceImp = UserServiceImp()) {
    suspend fun login(loginRequest: LoginRequest): Response<LoginResponse> {
        return service.login(loginRequest)
    }
}
