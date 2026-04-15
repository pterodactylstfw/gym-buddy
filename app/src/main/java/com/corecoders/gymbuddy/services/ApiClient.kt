package com.corecoders.gymbuddy.services

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // URL-ul de bază trebuie să se termine OBLIGATORIU cu /
    private const val BASE_URL = "https://edb-with-videos-and-images-by-ascendapi.p.rapidapi.com/"

    private val headerInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            .addHeader("X-RapidAPI-Key", "32769c9497msh48ca286b56b32cep111029jsn261023cab050")
            // 2. Schimbăm Host-ul
            .addHeader("X-RapidAPI-Host", "edb-with-videos-and-images-by-ascendapi.p.rapidapi.com")
            .build()
        chain.proceed(newRequest)
    }

    // (Opțional) Asta ne ajută să vedem în Logcat ce date primim de la server
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Configurăm curierul
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(headerInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    // Construim Retrofit-ul final
    val exerciseApi: ExerciseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // <-- Îi dăm curierul nostru modificat
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExerciseApiService::class.java)
    }
}