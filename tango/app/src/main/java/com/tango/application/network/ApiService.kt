package com.tango.application.network

import com.tango.application.data.AuthResponse
import com.tango.application.data.LoginRequest
import com.tango.application.data.RegisterRequest
import com.tango.application.data.User
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @GET("api/v1/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<User>
    
    @GET("health")
    suspend fun healthCheck(): Response<Map<String, String>>
} 