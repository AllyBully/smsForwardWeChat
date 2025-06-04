package com.ally.smsforward.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class SmsForwardRequest(
    val sender: String,
    val message: String,
    val wechatNumber: String
)

interface ApiService {
    @POST("api/sms/forward")
    suspend fun forwardSms(@Body request: SmsForwardRequest): retrofit2.Response<Unit> // Assuming server returns 200 OK with no body or a simple success message
}

object RetrofitClient {
    private const val BASE_URL = "http://localhost:8080/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}