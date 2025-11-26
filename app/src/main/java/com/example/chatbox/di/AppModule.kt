package com.example.chatbox.di

import android.content.Context
import com.example.chatbox.data.local.db.AppDatabase
import com.example.chatbox.data.remote.ChatApiService
import com.example.chatbox.data.repository.ChatRepositoryImpl
import com.example.chatbox.domain.repository.ChatRepository
import com.example.chatbox.domain.usecase.GetHistoryUseCase
import com.example.chatbox.domain.usecase.SendMessageUseCase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

// ✅ 智谱 API 基础配置（保持你之前调通的这套）
private const val ZHIPU_BASE_URL = "https://open.bigmodel.cn/api/"

// ⚠️ 把这里换成你自己的 API Key
private const val ZHIPU_API_KEY = "ed6a6b1fd3154139b438d05862734d31.uAsW6rz9m5ZXvoWj"

object AppModule {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // ========= Database =========

    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(appContext)
    }

    // ========= Moshi =========

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // ========= OkHttpClient（不再用 HttpLoggingInterceptor）=========

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // 给所有请求自动加上鉴权头和 JSON 头
            .addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val original = chain.request()

                    val newRequest = original.newBuilder()
                        .addHeader("Authorization", "Bearer $ZHIPU_API_KEY")
                        .addHeader("Content-Type", "application/json")
                        .build()

                    return chain.proceed(newRequest)
                }
            })
            .build()
    }

    // ========= Retrofit & ChatApiService =========

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ZHIPU_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private val chatApiService: ChatApiService by lazy {
        retrofit.create(ChatApiService::class.java)
    }

    // ========= Repository & UseCases =========

    private val chatRepositoryImpl: ChatRepositoryImpl by lazy {
        ChatRepositoryImpl(
            messageDao = database.messageDao(),
            api = chatApiService
        )
    }

    val chatRepository: ChatRepository
        get() = chatRepositoryImpl

    fun provideGetHistoryUseCase(): GetHistoryUseCase =
        GetHistoryUseCase(chatRepository)

    fun provideSendMessageUseCase(): SendMessageUseCase =
        SendMessageUseCase(chatRepository)
}
