package com.tango.application.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    // 開発用のベースURL
    private const val BASE_URL = "http://10.0.2.2:8080/"  // Android エミュレータ用（ホストマシンへの接続）
    // 実機の場合は "http://192.168.x.x:8080/" などローカルIPを指定
    // Windowsの場合、コマンドプロンプトで `ipconfig` で確認
    // macOS/Linuxの場合、ターミナルで `ifconfig` で確認
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
} 