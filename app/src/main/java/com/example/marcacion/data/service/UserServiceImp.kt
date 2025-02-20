package com.example.marcacion.data.service

import com.example.marcacion.data.dto.request.LoginRequest
import com.example.marcacion.data.dto.response.LoginResponse
import com.example.marcacion.data.dto.response.SearchDniResponse
import com.example.marcacion.data.utils.Constants
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class UserServiceImp {
    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create<UserService>()

    suspend fun login(loginRequest: LoginRequest): Response<LoginResponse> {
        return service.login(loginRequest)
    }

    suspend fun obtenerNombrePorDNI(dni: String): Response<SearchDniResponse> {
        return service.obtenerNombrePorDNI(dni)
    }
}
