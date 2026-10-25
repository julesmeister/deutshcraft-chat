package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.BuildConfig
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenAiGateway
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.julesmeister.deutschcraft.feature.chat.data.ai.DeutschCraftAiGateway
import com.julesmeister.deutschcraft.feature.messageactions.data.QwenSentenceInsightGateway
import com.julesmeister.deutschcraft.feature.messageactions.data.SentenceInsightGateway
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import com.julesmeister.deutschcraft.feature.chat.data.correction.MessageCorrectionGateway
import com.julesmeister.deutschcraft.feature.chat.data.correction.QwenMessageCorrectionGateway

@Module
@InstallIn(SingletonComponent::class)
abstract class AiBindingModule {

    @Binds
    @Singleton
    abstract fun bindDeutschCraftAiGateway(
        implementation: QwenAiGateway
    ): DeutschCraftAiGateway

    @Binds
    @Singleton
    abstract fun bindSentenceInsightGateway(
        implementation: QwenSentenceInsightGateway
    ): SentenceInsightGateway

    @Binds
    @Singleton
    abstract fun bindMessageCorrectionGateway(
        implementation:
        QwenMessageCorrectionGateway
    ): MessageCorrectionGateway
}

@Module
@InstallIn(SingletonComponent::class)
object AiNetworkModule {

    @Provides
    @Singleton
    fun provideQwenJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
            isLenient = false
        }
    }

    @Provides
    @Singleton
    fun provideQwenOkHttpClient():
            OkHttpClient {
        val loggingInterceptor =
            HttpLoggingInterceptor().apply {
                level =
                    if (BuildConfig.DEBUG) {
                        /*
                         * BASIC logs the request path and
                         * status but not learner messages.
                         */
                        HttpLoggingInterceptor
                            .Level
                            .BASIC
                    } else {
                        HttpLoggingInterceptor
                            .Level
                            .NONE
                    }
            }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest =
                    chain.request()

                val authenticatedRequest =
                    originalRequest
                        .newBuilder()
                        .header(
                            name = "Authorization",
                            value =
                                "Bearer " +
                                        BuildConfig
                                            .QWEN_API_KEY
                        )
                        .header(
                            name = "Content-Type",
                            value = "application/json"
                        )
                        .build()

                chain.proceed(
                    authenticatedRequest
                )
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(
                20,
                TimeUnit.SECONDS
            )
            .readTimeout(
                45,
                TimeUnit.SECONDS
            )
            .writeTimeout(
                20,
                TimeUnit.SECONDS
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideQwenRetrofit(
        json: Json,
        okHttpClient: OkHttpClient
    ): Retrofit {
        val contentType =
            "application/json"
                .toMediaType()

        return Retrofit.Builder()
            .baseUrl(
                BuildConfig.QWEN_BASE_URL
            )
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory(
                    contentType = contentType
                )
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideQwenApi(
        retrofit: Retrofit
    ): QwenApi {
        return retrofit.create(
            QwenApi::class.java
        )
    }


}