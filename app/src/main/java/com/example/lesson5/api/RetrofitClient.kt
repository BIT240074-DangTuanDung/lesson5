package com.example.lesson5.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Thay đổi URL này thành URL MockAPI của bạn (ví dụ: https://65b...mockapi.io/api/v1/)
    private const val BASE_URL = "https://6ace0155f338359cf1c2da91.mockapi.io/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
