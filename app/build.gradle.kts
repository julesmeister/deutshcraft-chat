// app/build.gradle.kts

import java.util.Properties

plugins {
    alias(
        libs.plugins.android.application
    )
    alias(
        libs.plugins.kotlin.compose
    )
    alias(
        libs.plugins.kotlin.serialization
    )
    alias(
        libs.plugins.ksp
    )
    alias(
        libs.plugins.hilt
    )
}

val localProperties =
    Properties().apply {
        val localPropertiesFile =
            rootProject.file(
                "local.properties"
            )

        if (
            localPropertiesFile.exists()
        ) {
            localPropertiesFile
                .inputStream()
                .use(::load)
        }
    }

fun normalizedBaseUrl(
    propertyName: String
): String {
    return localProperties
        .getProperty(
            propertyName
        )
        .orEmpty()
        .trim()
        .let { url ->
            when {
                url.isEmpty() ->
                    ""

                url.endsWith("/") ->
                    url

                else ->
                    "$url/"
            }
        }
}

fun localProperty(
    name: String,
    default: String = ""
): String {
    return localProperties
        .getProperty(
            name
        )
        .orEmpty()
        .trim()
        .ifEmpty {
            default
        }
}

val aiProxyBaseUrl =
    normalizedBaseUrl(
        propertyName =
            "AI_PROXY_BASE_URL"
    )

val cognitoRegion =
    localProperty(
        name =
            "COGNITO_REGION",
        default =
            "us-east-1"
    )

val cognitoUserPoolId =
    localProperty(
        name =
            "COGNITO_USER_POOL_ID"
    )

val cognitoClientId =
    localProperty(
        name =
            "COGNITO_CLIENT_ID"
    )

val cognitoDomainPrefix =
    localProperty(
        name =
            "COGNITO_DOMAIN_PREFIX"
    )

android {
    namespace =
        "com.julesmeister.deutschcraft"

    compileSdk {
        version =
            release(37)
    }

    defaultConfig {
        applicationId =
            "com.julesmeister.deutschcraft"

        minSdk =
            24

        targetSdk =
            37

        versionCode =
            1

        versionName =
            "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            type =
                "String",
            name =
                "QWEN_BASE_URL",
            value =
                "\"$aiProxyBaseUrl\""
        )

        buildConfigField(
            type =
                "String",
            name =
                "QWEN_MODEL",
            value =
                "\"qwen3.7-flash\""
        )

        buildConfigField(
            type =
                "String",
            name =
                "COGNITO_REGION",
            value =
                "\"$cognitoRegion\""
        )

        buildConfigField(
            type =
                "String",
            name =
                "COGNITO_USER_POOL_ID",
            value =
                "\"$cognitoUserPoolId\""
        )

        buildConfigField(
            type =
                "String",
            name =
                "COGNITO_CLIENT_ID",
            value =
                "\"$cognitoClientId\""
        )

        buildConfigField(
            type =
                "String",
            name =
                "COGNITO_DOMAIN_PREFIX",
            value =
                "\"$cognitoDomainPrefix\""
        )
    }

    buildTypes {
        release {
            optimization {
                enable =
                    true

                packageScope =
                    setOf(
                        "androidx.**",
                        "kotlin.**",
                        "kotlinx.**"
                    )
            }
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled =
            true

        sourceCompatibility =
            JavaVersion.VERSION_17

        targetCompatibility =
            JavaVersion.VERSION_17
    }

    buildFeatures {
        compose =
            true

        buildConfig =
            true
    }
}

dependencies {
    implementation(
        "androidx.hilt:hilt-work:1.2.0"
    )

    ksp(
        "androidx.hilt:hilt-compiler:1.2.0"
    )

    implementation(
        "androidx.compose.runtime:runtime:1.9.4"
    )

    implementation(
        "androidx.compose.runtime:runtime-saveable:1.9.4"
    )

    implementation(
        "io.github.kyant0:backdrop:1.0.0"
    )

    implementation(
        "androidx.datastore:datastore-preferences:1.1.7"
    )

    implementation(
        "androidx.work:work-runtime-ktx:2.10.4"
    )

    implementation(
        "androidx.browser:browser:1.8.0"
    )

    implementation(
        "com.squareup.retrofit2:retrofit:2.11.0"
    )

    implementation(
        "com.jakewharton.retrofit:" +
                "retrofit2-kotlinx-serialization-converter:" +
                "1.0.0"
    )

    implementation(
        "com.squareup.okhttp3:okhttp:4.12.0"
    )

    implementation(
        "com.squareup.okhttp3:" +
                "logging-interceptor:4.12.0"
    )

    implementation(
        libs.androidx.compose.animation.core
    )

    implementation(
        libs.androidx.compose.runtime
    )

    implementation(
        libs.androidx.compose.runtime.saveable
    )

    implementation(
        libs.androidx.compose.ui.text
    )

    implementation(
        libs.hilt.android
    )

    ksp(
        libs.hilt.compiler
    )

    implementation(
        libs.androidx.room.runtime
    )

    implementation(
        libs.androidx.room.ktx
    )

    ksp(
        libs.androidx.room.compiler
    )

    implementation(
        libs.kotlinx.serialization.json
    )

    implementation(
        libs.androidx.navigation.compose
    )

    implementation(
        "androidx.hilt:" +
                "hilt-navigation-compose:1.2.0"
    )

    implementation(
        "androidx.compose.material:" +
                "material-icons-core"
    )

    implementation(
        "androidx.compose.material:" +
                "material-icons-extended"
    )

    implementation(
        libs.androidx.compose.material.icons.extended
    )

    implementation(
        libs.androidx.lifecycle.runtime.compose
    )

    implementation(
        libs.androidx.core.ktx
    )

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )

    implementation(
        libs.androidx.activity.compose
    )

    implementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    implementation(
        libs.androidx.compose.ui
    )

    implementation(
        libs.androidx.compose.ui.graphics
    )

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )

    implementation(
        libs.androidx.compose.material3
    )

    implementation(
        "androidx.lifecycle:" +
                "lifecycle-viewmodel-compose:2.9.4"
    )

    implementation(
        "org.jetbrains.kotlinx:" +
                "kotlinx-coroutines-android:1.10.2"
    )

    testImplementation(
        libs.junit
    )

    androidTestImplementation(
        libs.androidx.junit
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )

    coreLibraryDesugaring(
        "com.android.tools:" +
                "desugar_jdk_libs:2.1.5"
    )
}

ksp {
    arg(
        "room.schemaLocation",
        "$projectDir/schemas"
    )
}