package com.tango.application.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    // 実機用の設定
    // 以下のいずれかを使用してください：
    // 1. リモートサーバー
    private const val REMOTE_BASE_URL = "http://tango.fumi042-server.top/"
    // 2. ローカルサーバー（開発時）- 実際のPCのIPアドレスに変更してください
    // private const val LOCAL_BASE_URL = "http://192.168.1.100:8080/"  // 実際のIPアドレスに変更
    
    // 現在使用するURL（開発時はここを変更してください）
    private const val BASE_URL = REMOTE_BASE_URL
    
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("TangoNetwork", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
    
    // デバッグ用：現在のベースURLを取得
    fun getCurrentBaseUrl(): String = BASE_URL
} 