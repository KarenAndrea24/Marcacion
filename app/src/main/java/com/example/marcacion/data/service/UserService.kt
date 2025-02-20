package com.example.marcacion.data.service

import com.example.marcacion.data.dto.request.LoginRequest
import com.example.marcacion.data.dto.response.LoginResponse
import com.example.marcacion.data.dto.response.SearchDniResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {
    @POST("/api/web/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @GET("api/web/marcaciones/{dni}/dni")
    suspend fun obtenerNombrePorDNI(@Path("dni") dni: String): Response<SearchDniResponse>
}
