// app/src/main/java/com/julesmeister/deutschcraft/core/di/AiModule.kt

package com.julesmeister.deutschcraft.core.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.julesmeister.deutschcraft.BuildConfig
import com.julesmeister.deutschcraft.feature.chat.data.ai.DeutschCraftAiGateway
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenAiGateway
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenApi
import com.julesmeister.deutschcraft.feature.chat.data.correction.MessageCorrectionGateway
import com.julesmeister.deutschcraft.feature.chat.data.correction.QwenMessageCorrectionGateway
import com.julesmeister.deutschcraft.feature.messageactions.data.QwenSentenceInsightGateway
import com.julesmeister.deutschcraft.feature.messageactions.data.SentenceInsightGateway
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

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
        implementation: QwenMessageCorrectionGateway
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
    fun provideQwenOkHttpClient(): OkHttpClient {
        val loggingInterceptor =
            HttpLoggingInterceptor().apply {
                level =
                    if (BuildConfig.DEBUG) {
                        /*
                         * BASIC logs the request path and status
                         * but not learner messages.
                         */
                        HttpLoggingInterceptor.Level.BASIC
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
            }

        return OkHttpClient.Builder()
            /*
             * No Authorization header.
             *
             * Requests go to the DeutschCraft AI proxy
             * (API Gateway -> Lambda). The Lambda reads the Qwen
             * key from AWS Secrets Manager and adds it upstream.
             *
             * Step 2 (Cognito) will add the user's JWT here.
             */
            .addInterceptor { chain ->
                val request =
                    chain.request()
                        .newBuilder()
                        .header(
                            name = "Content-Type",
                            value = "application/json"
                        )
                        .build()

                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            /*
             * API Gateway HTTP APIs time out at 30 seconds, and the
             * Lambda timeout is 29 seconds.
             */
            .readTimeout(35, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideQwenRetrofit(
        json: Json,
        okHttpClient: OkHttpClient
    ): Retrofit {
        val contentType =
            "application/json".toMediaType()

        /*
         * Retrofit throws at startup for a blank base URL. Use a
         * placeholder so the app still launches; the gateways
         * report the missing proxy URL to the user instead.
         */
        val baseUrl =
            BuildConfig.QWEN_BASE_URL
                .ifBlank { MISSING_PROXY_PLACEHOLDER_URL }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
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
        return retrofit.create(QwenApi::class.java)
    }

    private const val MISSING_PROXY_PLACEHOLDER_URL =
        "https://ai-proxy.invalid/"
}