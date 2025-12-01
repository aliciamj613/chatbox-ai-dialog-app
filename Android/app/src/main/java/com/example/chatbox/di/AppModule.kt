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

object AppModule {

    // 智谱 API 基础地址
    private const val BASE_URL = "https://open.bigmodel.cn/api/"

    // ✅ 把这里换成你自己的智谱 API Key
    // 比如：const val ZHIPU_API_KEY = "sk-xxxx"
    private const val ZHIPU_API_KEY = ""

    // Application Context
    private lateinit var appContext: Context

    /**
     * 在 Application.onCreate() 里调用
     * ChatApplication / ChatboxApp 已经有：AppModule.init(this)
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // ----------------- Database -----------------

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(appContext)
    }

    // ----------------- Network ------------------

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    /** 给所有请求加上 Authorization 头 */
    private val authInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val newRequest = original.newBuilder()
                .header("Authorization", "Bearer $ZHIPU_API_KEY")
                .build()
            return chain.proceed(newRequest)
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
    }

    private val chatApiService: ChatApiService by lazy {
        retrofit.create(ChatApiService::class.java)
    }

    // ----------------- Repository & UseCases -----------------

    private val chatRepositoryImpl: ChatRepositoryImpl by lazy {
        ChatRepositoryImpl(
            messageDao = database.messageDao(),
            conversationDao = database.conversationDao(),
            api = chatApiService
        )
    }

    val chatRepository: ChatRepository
        get() = chatRepositoryImpl

    fun provideGetHistoryUseCase(): GetHistoryUseCase =
        GetHistoryUseCase(chatRepositoryImpl)

    fun provideSendMessageUseCase(): SendMessageUseCase =
        SendMessageUseCase(chatRepositoryImpl)
}
