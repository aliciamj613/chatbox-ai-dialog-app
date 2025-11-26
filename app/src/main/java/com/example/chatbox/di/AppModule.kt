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
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

// 智谱 API 基础地址
private const val ZHIPU_BASE_URL = "https://open.bigmodel.cn/api/"

// ⚠️ 这里一定要改成你控制台里的真实 key！！不要带 "Bearer " 前缀
// 比如： private const val ZHIPU_API_KEY = "sk-xxxxxxxxxxxxxxxx"
private const val ZHIPU_API_KEY = "ed6a6b1fd3154139b438d05862734d31.uAsW6rz9m5ZXvoWj"

object AppModule {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // ========== Database ==========

    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(appContext)
    }

    // ========== Moshi / OkHttp / Retrofit / ChatApiService ==========

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()

                // 在每个请求上自动加上鉴权头和 JSON 头
                val newRequest = original.newBuilder()
                    .addHeader("Authorization", "Bearer $ZHIPU_API_KEY")
                    .addHeader("Content-Type", "application/json")
                    .build()

                chain.proceed(newRequest)
            }
            .build()
    }

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

    // ========== Repository & UseCases ==========

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
