package com.julesmeister.deutschcraft.feature.auth.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.julesmeister.deutschcraft.BuildConfig
import com.julesmeister.deutschcraft.feature.auth.data.CognitoAuthRepository
import com.julesmeister.deutschcraft.feature.auth.data.CognitoConfig
import com.julesmeister.deutschcraft.feature.auth.data.remote.CognitoAuthApi
import com.julesmeister.deutschcraft.feature.auth.data.remote.CognitoOAuthApi
import com.julesmeister.deutschcraft.feature.auth.domain.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CognitoHttpClient

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindingModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        implementation: CognitoAuthRepository
    ): AuthRepository
}

/*
 * Cognito gets its own OkHttp client: it must never carry the
 * AI proxy's Bearer header or its 401 authenticator, otherwise a
 * refresh call could recurse into itself.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthNetworkModule {

    @Provides
    @Singleton
    @CognitoHttpClient
    fun provideCognitoOkHttpClient(): OkHttpClient {
        val loggingInterceptor =
            HttpLoggingInterceptor().apply {
                // Never log bodies: they contain passwords and tokens.
                level =
                    if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BASIC
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
            }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideCognitoAuthApi(
        config: CognitoConfig,
        json: Json,
        @CognitoHttpClient okHttpClient: OkHttpClient
    ): CognitoAuthApi =
        Retrofit.Builder()
            .baseUrl(config.identityProviderBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory(
                    contentType = CognitoAuthApi.ContentType.toMediaType()
                )
            )
            .build()
            .create(CognitoAuthApi::class.java)

    @Provides
    @Singleton
    fun provideCognitoOAuthApi(
        config: CognitoConfig,
        json: Json,
        @CognitoHttpClient okHttpClient: OkHttpClient
    ): CognitoOAuthApi =
        Retrofit.Builder()
            .baseUrl(config.hostedUiBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory(
                    contentType = "application/json".toMediaType()
                )
            )
            .build()
            .create(CognitoOAuthApi::class.java)
}
